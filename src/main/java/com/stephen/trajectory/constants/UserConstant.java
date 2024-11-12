package com.stephen.trajectory.constants;

/**
 * 用户常量
 *
 * @author stephen qiu
 */
public interface UserConstant {
	
	/**
	 * 用户登录态键
	 */
	String USER_LOGIN_STATE = "user_login";
	
	/**
	 * 用户默认登录密码
	 */
	String DEFAULT_PASSWORD = "12345678";
	
	/**
	 * 用户默认头像地址
	 */
	String USER_AVATAR = "https://butterfly-1318299170.cos.ap-shanghai.myqcloud.com/logo/avatar/default_avatar.png";
	
	
	//  region 权限
	
	/**
	 * 默认角色
	 */
	String DEFAULT_ROLE = "user";
	
	/**
	 * 管理员角色
	 */
	String ADMIN_ROLE = "admin";
	
	/**
	 * 被封号
	 */
	String BAN_ROLE = "ban";
	
	// endregion
}
