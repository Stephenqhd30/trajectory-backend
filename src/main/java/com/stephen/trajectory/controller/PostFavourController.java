package com.stephen.trajectory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.dto.postFavour.PostFavourAddRequest;
import com.stephen.trajectory.model.dto.postFavour.PostFavourQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.service.PostFavourService;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子收藏接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/post_favour")
@Slf4j
public class PostFavourController {
	
	@Resource
	private PostFavourService postFavourService;
	
	@Resource
	private PostService postService;
	
	@Resource
	private UserService userService;
	
	/**
	 * 收藏 / 取消收藏
	 *
	 * @param postFavourAddRequest postFavourAddRequest
	 * @param request              request
	 * @return BaseResponse<Integer>
	 */
	@PostMapping("/")
	public BaseResponse<Integer> doPostFavour(@RequestBody PostFavourAddRequest postFavourAddRequest,
	                                          HttpServletRequest request) {
		if (postFavourAddRequest == null || postFavourAddRequest.getPostId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 登录才能操作
		final User loginUser = userService.getLoginUser(request);
		long postId = postFavourAddRequest.getPostId();
		int result = postFavourService.doPostFavour(postId, loginUser);
		return ResultUtils.success(result);
	}
	
	/**
	 * 获取我收藏的帖子列表
	 *
	 * @param postQueryRequest postQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/my/list/page")
	public BaseResponse<Page<PostVO>> listMyFavourPostByPage(@RequestBody PostQueryRequest postQueryRequest,
	                                                         HttpServletRequest request) {
		if (postQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long current = postQueryRequest.getCurrent();
		long size = postQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
				postService.getQueryWrapper(postQueryRequest), loginUser.getId());
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
	/**
	 * 获取用户收藏的帖子列表
	 *
	 * @param postFavourQueryRequest postFavourQueryRequest
	 * @param request                request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/list/page")
	public BaseResponse<Page<PostVO>> listFavourPostByPage(@RequestBody PostFavourQueryRequest postFavourQueryRequest,
	                                                       HttpServletRequest request) {
		if (postFavourQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = postFavourQueryRequest.getCurrent();
		long size = postFavourQueryRequest.getPageSize();
		Long userId = postFavourQueryRequest.getUserId();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postFavourService.listFavourPostByPage(new Page<>(current, size),
				postService.getQueryWrapper(postFavourQueryRequest.getPostQueryRequest()), userId);
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
}
