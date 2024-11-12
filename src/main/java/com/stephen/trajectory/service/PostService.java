package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 * @author stephen qiu
 */
public interface PostService extends IService<Post> {
	
	/**
	 * 校验
	 *
	 * @param post post
	 * @param add  add
	 */
	void validPost(Post post, boolean add);
	
	/**
	 * 获取查询条件
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return {@link QueryWrapper<Post>}
	 */
	QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);
	
	/**
	 * 获取帖子封装
	 *
	 * @param post    post
	 * @param request request
	 * @return {@link PostVO}
	 */
	PostVO getPostVO(Post post, HttpServletRequest request);
	
	/**
	 * 分页获取帖子封装
	 *
	 * @param postPage postPage
	 * @param request  request
	 * @return {@link Page<PostVO>}
	 */
	Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);
}
