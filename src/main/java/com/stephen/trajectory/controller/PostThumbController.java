package com.stephen.trajectory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.dto.postThumb.PostThumbAddRequest;
import com.stephen.trajectory.model.dto.postThumb.PostThumbQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.PostThumbService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {
	
	@Resource
	private PostThumbService postThumbService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private PostService postService;
	
	/**
	 * 点赞 / 取消点赞
	 *
	 * @param postThumbAddRequest postThumbAddRequest
	 * @param request             request
	 * @return BaseResponse<Integer> resultNum 本次点赞变化数
	 */
	@PostMapping("/")
	public BaseResponse<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
	                                     HttpServletRequest request) {
		if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 登录才能点赞
		final User loginUser = userService.getLoginUser(request);
		long postId = postThumbAddRequest.getPostId();
		int result = postThumbService.doPostThumb(postId, loginUser);
		return ResultUtils.success(result);
	}
	
	/**
	 * 获取我点赞的帖子列表
	 *
	 * @param postQueryRequest postQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/my/list/page")
	public BaseResponse<Page<PostVO>> listMyThumbPostByPage(@RequestBody PostQueryRequest postQueryRequest,
	                                                        HttpServletRequest request) {
		if (postQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long current = postQueryRequest.getCurrent();
		long size = postQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postThumbService.listThumbPostByPage(new Page<>(current, size),
				postService.getQueryWrapper(postQueryRequest), loginUser.getId());
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
	/**
	 * 获取用户点赞的帖子列表
	 *
	 * @param postThumbQueryRequest postThumbQueryRequest
	 * @param request               request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/list/page")
	public BaseResponse<Page<PostVO>> listThumbPostByPage(@RequestBody PostThumbQueryRequest postThumbQueryRequest,
	                                                      HttpServletRequest request) {
		if (postThumbQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = postThumbQueryRequest.getCurrent();
		long size = postThumbQueryRequest.getPageSize();
		Long userId = postThumbQueryRequest.getUserId();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postThumbService.listThumbPostByPage(new Page<>(current, size),
				postService.getQueryWrapper(postThumbQueryRequest.getPostQueryRequest()), userId);
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
}
