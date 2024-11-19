package com.stephen.trajectory.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.PostThumb;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞数据库操作
 *
 * @author stephen qiu
 */
public interface PostThumbMapper extends BaseMapper<PostThumb> {
	
	/**
	 * 分页查询点赞帖子列表
	 *
	 * @param page         page
	 * @param queryWrapper queryWrapper
	 * @param thumbUserId favourUserId
	 * @return {@link Page < Post >}
	 */
	Page<Post> listThumbPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
	                               long thumbUserId);
}




