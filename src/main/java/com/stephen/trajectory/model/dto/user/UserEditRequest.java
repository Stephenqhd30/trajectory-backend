package com.stephen.trajectory.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户更新个人信息请求
 *
 * @author stephen qiu
 */
@Data
public class UserEditRequest implements Serializable {
	
	private static final long serialVersionUID = 402901746420005392L;
	/**
	 * 用户昵称
	 */
	private String userName;
	
	/**
	 * 用户密码
	 */
	private String userPassword;
	
	/**
	 * 用户头像
	 */
	private String userAvatar;
	
	/**
	 * 用户邮箱
	 */
	private String userEmail;
	
	/**
	 * 手机号码
	 */
	private String userPhone;
	
	/**
	 * 标签列表(使用JSON字符数组)
	 */
	private List<String> tags;
	
	
}