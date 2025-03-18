package com.stephen.trajectory.model.vo;

import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 用户视图（脱敏）
 *
 * @author stephen qiu
 */
@Data
public class UserVO implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 用户昵称
	 */
	private String userName;
	
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
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 编辑时间
	 */
	private Date editTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	
	/**
	 * 标签列表(使用JSON字符数组)
	 */
	private List<String> tags;
	
	/**
	 * 相似度
	 */
	private Double similarity;
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 封装类转对象
	 *
	 * @param userVO userVO
	 * @return User
	 */
	public static User voToObj(UserVO userVO) {
		if (userVO == null) {
			return null;
		}
		// todo 需要进行转换
		User user = new User();
		BeanUtils.copyProperties(userVO, user);
		List<String> tagList = userVO.getTags();
		user.setTags(Optional.ofNullable(JSONUtil.toJsonStr(tagList))
				.orElse(""));
		return user;
	}
	
	/**
	 * 对象转封装类
	 *
	 * @param user user
	 * @return UserVO
	 */
	public static UserVO objToVo(User user) {
		if (user == null) {
			return null;
		}
		// todo 需要进行转换
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		String tags = user.getTags();
		userVO.setTags(Optional.ofNullable(JSONUtil.toList(tags, String.class))
				.orElse(new ArrayList<>()));
		
		return userVO;
	}
}