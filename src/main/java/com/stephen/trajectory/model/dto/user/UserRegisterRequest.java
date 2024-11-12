package com.stephen.trajectory.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author stephen qiu
 */
@Data
public class UserRegisterRequest implements Serializable {
	
	private static final long serialVersionUID = 3191241716373120793L;
	
	/**
	 * 账号
	 */
	private String userAccount;
	
	/**
	 * 密码
	 */
	private String userPassword;
	
	/**
	 * 再次输入密码
	 */
	private String checkPassword;
}
