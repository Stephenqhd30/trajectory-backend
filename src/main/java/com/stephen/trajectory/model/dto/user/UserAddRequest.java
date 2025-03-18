package com.stephen.trajectory.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户创建请求
 *
 * @author stephen qiu
 */
@Data
public class UserAddRequest implements Serializable {
	
	private static final long serialVersionUID = -6510457969873015318L;
	/**
	 * 用户昵称
	 */
	private String userName;
	
	/**
	 * 账号
	 */
	private String userAccount;
	
	/**
	 * 用户头像
	 */
	private String userAvatar;
	
	/**
	 * 用户角色: user, admin
	 */
	private String userRole;
	
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