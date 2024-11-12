package com.stephen.trajectory.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.PostFavour;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子收藏数据库操作
 *
 * @author stephen qiu
 */
public interface PostFavourMapper extends BaseMapper<PostFavour> {
	
	/**
	 * 分页查询收藏帖子列表
	 *
	 * @param page         page
	 * @param queryWrapper queryWrapper
	 * @param favourUserId favourUserId
	 * @return {@link Page<Post>}
	 */
	Page<Post> listFavourPostByPage(IPage<Post> page, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper,
	                                long favourUserId);
	
}




