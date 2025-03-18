package com.stephen.trajectory.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户更新请求
 *
 * @author stephen qiu
 */
@Data
public class UserUpdateRequest implements Serializable {
	/**
	 * id
	 */
	private Long id;
	
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
	 * 用户角色：user/admin/ban
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
	
	
	private static final long serialVersionUID = 1L;
}