package com.stephen.trajectory.config.secure;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.config.secure.condition.SaCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * SaToken认证配置
 *
 * @author stephen qiu
 */
@Configuration
@Conditional(SaCondition.class)
@Slf4j
public class SaTokenConfiguration implements WebMvcConfigurer {
	
	/**
	 * 定义SaToken不需要拦截的URI
	 */
	private static final List<String> SA_TOKEN_NOT_NEED_INTERCEPT_URI = new ArrayList<>() {
		private static final long serialVersionUID = 5839574116900754104L;
		
		{
			add("/");
			add("/user/register");
			add("/user/login");
			add("/user/get/login");
			add("/post/list/page/vo");
			add("/chart/list/page/vo");
			add("/swagger-ui/**");
			add("/v2/api-docs/**");
			add("/swagger-resources/**");
			add("/webjars/**");
			add("/resources/**");
			
		}
	};
	
	/**
	 * 注册sa-token的拦截器
	 *
	 * @param registry 拦截器注册器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 注册路由拦截器，自定义验证规则
		registry.addInterceptor(new SaInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns(SA_TOKEN_NOT_NEED_INTERCEPT_URI);
	}
	
	/**
	 * 静态资源映射
	 *
	 * @param registry 静态资源注册器
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 映射Swagger静态资源
		registry.addResourceHandler("/swagger-ui/**")
				.addResourceLocations("classpath:/META-INF/resources/swagger-ui/");
		registry.addResourceHandler("/resources/**")
				.addResourceLocations("classpath:/resources/");
	}
	
	/**
	 * 注册 [Sa-Token全局过滤器]
	 */
	@Bean
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter()
				// 异常处理函数：每次认证函数发生异常时执行此函数
				.setError(e -> {
					// 设置响应头
					SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
					// 使用封装的 JSON 工具类转换数据格式
					return JSONUtil.toJsonStr(SaResult.error(e.getMessage()));
				})
				// 前置函数：在每次认证函数之前执行（BeforeAuth 不受 includeList 与 excludeList 的限制，所有请求都会进入）
				.setBeforeAuth(r -> {
					// ---------- 设置一些安全响应头 ----------
					SaHolder.getResponse()
							// 服务器名称
							.setServer("sa-server")
							// 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
							.setHeader("X-Frame-Options", "SAMEORIGIN")
							// 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
							.setHeader("X-XSS-Protection", "1; mode=block")
							// 禁用浏览器内容嗅探
							.setHeader("X-Content-Type-Options", "nosniff")
					;
				});
		
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
	
}