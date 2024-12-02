package com.stephen.trajectory.elasticsearch.modal.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.model.entity.User;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 用户 Elasticsearch DTO
 *
 * @author stephen qiu
 */
@Data
// todo 取消注释开启 ES（须先配置 ES）
@Document(indexName = "trajectory_user", createIndex = false)
public class UserEsDTO {
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	/**
	 * 用户ID
	 */
	@Id
	private Long id;
	
	/**
	 * 用户账号
	 */
	private String userAccount;
	
	/**
	 * 用户昵称
	 */
	private String userName;
	
	/**
	 * 用户头像
	 */
	private String userAvatar;
	
	/**
	 * 性别（0-男，1-女，2-保密）
	 */
	private Integer userGender;
	
	/**
	 * 用户简介
	 */
	private String userProfile;
	
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
	@Field(type = FieldType.Keyword)
	private List<String> tags;
	
	/**
	 * 编辑时间
	 */
	@Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
	private Date editTime;
	
	/**
	 * 创建时间
	 */
	@Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
	private Date updateTime;
	
	/**
	 * 是否删除
	 */
	private Integer isDelete;
	
	/**
	 * 对象转包装类
	 *
	 * @param user user
	 * @return {@link UserEsDTO}
	 */
	public static UserEsDTO objToDto(User user) {
		if (user == null) {
			return null;
		}
		UserEsDTO userEsDTO = new UserEsDTO();
		BeanUtils.copyProperties(user, userEsDTO);
		String tagsStr = user.getTags();
		if (StringUtils.isNotBlank(tagsStr)) {
			userEsDTO.setTags(JSONUtil.toList(tagsStr, String.class));
		}
		return userEsDTO;
	}
	
	/**
	 * 包装类转对象
	 *
	 * @param userEsDTO userEsDTO
	 * @return {@link User}
	 */
	public static User dtoToObj(UserEsDTO userEsDTO) {
		if (userEsDTO == null) {
			return null;
		}
		User user = new User();
		BeanUtils.copyProperties(userEsDTO, user);
		List<String> tagList = userEsDTO.getTags();
		if (CollUtil.isNotEmpty(tagList)) {
			user.setTags(JSONUtil.toJsonStr(tagList));
		}
		return user;
	}
}
