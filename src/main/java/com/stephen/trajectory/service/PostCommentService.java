package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.dto.postComment.PostCommentQueryRequest;
import com.stephen.trajectory.model.entity.PostComment;
import com.stephen.trajectory.model.vo.PostCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子评论服务
 *
 * @author stephen qiu
 */
public interface PostCommentService extends IService<PostComment> {
	
	/**
	 * 校验数据
	 *
	 * @param postComment postComment
	 * @param add         对创建的数据进行校验
	 */
	void validPostComment(PostComment postComment, boolean add);
	
	/**
	 * 获取查询条件
	 *
	 * @param postCommentQueryRequest postCommentQueryRequest
	 * @return {@link QueryWrapper<PostComment>}
	 */
	QueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest postCommentQueryRequest);
	
	/**
	 * 获取帖子评论封装
	 *
	 * @param postComment postComment
	 * @param request     request
	 * @return {@link PostCommentVO}
	 */
	PostCommentVO getPostCommentVO(PostComment postComment, HttpServletRequest request);
	
	/**
	 * 分页获取帖子评论封装
	 *
	 * @param postCommentPage postCommentPage
	 * @param request         request
	 * @return {@link Page<PostCommentVO>}
	 */
	Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> postCommentPage, HttpServletRequest request);
}