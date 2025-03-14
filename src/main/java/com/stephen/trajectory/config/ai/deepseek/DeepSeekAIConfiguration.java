package com.stephen.trajectory.config.ai.deepseek;

import com.stephen.trajectory.config.ai.deepseek.condition.DeepSeekAICondition;
import com.stephen.trajectory.config.ai.deepseek.properties.DeepSeekAIProperties;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeekAI调用
 *
 * @author: stephen qiu
 **/
@Configuration
@Slf4j
@Conditional(DeepSeekAICondition.class)
public class DeepSeekAIConfiguration {
	
	@Resource
	private DeepSeekAIProperties properties;
	
	
	@Bean
	public ArkService service() {
		ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
		Dispatcher dispatcher = new Dispatcher();
		return ArkService.builder()
				.dispatcher(dispatcher)
				.connectionPool(connectionPool)
				.baseUrl(properties.getBaseUrl())
				.apiKey(properties.getApiKey())
				.build();
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
}
