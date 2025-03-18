package com.stephen.trajectory.model.dto.user;

import com.stephen.trajectory.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 用户查询请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
	private static final long serialVersionUID = 8796619426266616906L;
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * id
	 */
	private Long notId;
	
	/**
	 * 用户昵称
	 */
	private String userName;
	
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
	
	/**
	 * 至少有一个标签
	 */
	private List<String> orTags;
	
	/**
	 * 搜索关键词
	 */
	private String searchText;
	
}