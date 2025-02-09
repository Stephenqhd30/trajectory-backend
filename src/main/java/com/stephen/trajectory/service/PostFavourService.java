package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.PostFavour;
import com.stephen.trajectory.model.entity.User;

/**
 * 帖子收藏服务
 *
 * @author stephen qiu
 */
public interface PostFavourService extends IService<PostFavour> {
	
	/**
	 * 帖子收藏
	 *
	 * @param postId    postId
	 * @param loginUser loginUser
	 * @return int
	 */
	int doPostFavour(long postId, User loginUser);
	
	/**
	 * 分页获取用户收藏的帖子列表
	 *
	 * @param page         page
	 * @param queryWrapper queryWrapper
	 * @param favourUserId favourUserId
	 * @return Page<Post>
	 */
	Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper,
	                                long favourUserId);
	
	/**
	 * 帖子收藏（内部服务）
	 *
	 * @param userId userId
	 * @param postId postId
	 * @return int
	 */
	int doPostFavourInner(long userId, long postId);
}
