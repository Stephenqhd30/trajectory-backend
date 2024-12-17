package com.stephen.trajectory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;

import java.util.Date;
import java.util.List;

/**
 * 用户数据库操作
 *
 * @author stephen qiu
 */
public interface UserMapper extends BaseMapper<User> {
	
	/**
	 * 查询用户列表（包括已被删除的数据）
	 */
	List<User> listUserWithDelete(Date minUpdateTime);

}




