package com.stephen.trajectory.config.secure;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.stephen.trajectory.config.secure.condition.SaCondition;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SaToken鉴权配置
 * 这里应该会依靠loginId对数据库进行相关查询，得到的结果放入结果集中，
 * 该模板不会单独规划处一个权限或者角色数据库表样式，而是将角色内嵌于模板SQL中t_user表内，
 * 因为该模板主要针对于不同的中小系统而设计的，不同系统都有不同的权限和角色分配，
 * 用与不用SaToken鉴权完全取决于系统本身业务，所以此处@Component注解打开与否完全取决于开发者；
 *
 * @author stephen qiu
 */
@Configuration
@Conditional(SaCondition.class)
@Slf4j
public class AuthorizationConfiguration implements StpInterface {
	
	/**
	 * 重写权限方法
	 *
	 * @param loginId   账号id
	 * @param loginType 账号类型
	 * @return 返回结果
	 */
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		// 根据SaToken权限配置文档：https://sa-token.cc/doc.html#/use/jur-auth
		// 由于此处设计主要针对于接口权限，所以权限通常有多个，上帝权限和个别极端情况除外
		// 举例：User实体中有一个add方法()，则推荐将该方法权限写为"user.add"，支持通配符操作，如果想要得到User实体类中所有方法的调用权限，则写为"user.*"
		// "*"表示上帝权限
		return new ArrayList<>();
	}
	
	/**
	 * 重写角色方法
	 *
	 * @param loginId   账号id
	 * @param loginType 账号类型
	 * @return 返回结果
	 */
	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		// 根据SaToken权限配置文档：https://sa-token.cc/doc.html#/use/jur-auth
		// 由于此处设计主要针对于用户角色，所以角色通常只有一个，个别情况除外
		// "*"表示上帝角色
		SaSession saSession = StpUtil.getSessionByLoginId(loginId);
		User user = (User) saSession.get(UserConstant.USER_LOGIN_STATE);
		return Collections.singletonList(user.getUserRole());
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
	
}