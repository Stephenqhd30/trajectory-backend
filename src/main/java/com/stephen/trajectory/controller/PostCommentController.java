package com.stephen.trajectory.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.dto.postComment.PostCommentAddRequest;
import com.stephen.trajectory.model.dto.postComment.PostCommentEditRequest;
import com.stephen.trajectory.model.dto.postComment.PostCommentQueryRequest;
import com.stephen.trajectory.model.dto.postComment.PostCommentUpdateRequest;
import com.stephen.trajectory.model.entity.PostComment;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostCommentVO;
import com.stephen.trajectory.service.PostCommentService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子评论接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/postComment")
@Slf4j
public class PostCommentController {
	
	@Resource
	private PostCommentService postCommentService;
	
	@Resource
	private UserService userService;
	
	// region 增删改查
	
	/**
	 * 创建帖子评论
	 *
	 * @param postCommentAddRequest postCommentAddRequest
	 * @param request               request
	 * @return {@link BaseResponse<Long>}
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addPostComment(@RequestBody PostCommentAddRequest postCommentAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(postCommentAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		PostComment postComment = new PostComment();
		BeanUtils.copyProperties(postCommentAddRequest, postComment);
		// 数据校验
		try {
			postCommentService.validPostComment(postComment, true);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		postComment.setUserId(loginUser.getId());
		// 写入数据库
		boolean result = postCommentService.save(postComment);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newPostCommentId = postComment.getId();
		return ResultUtils.success(newPostCommentId);
	}
	
	/**
	 * 删除帖子评论
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePostComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		PostComment oldPostComment = postCommentService.getById(id);
		ThrowUtils.throwIf(oldPostComment == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldPostComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = postCommentService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新帖子评论（仅管理员可用）
	 *
	 * @param postCommentUpdateRequest postCommentUpdateRequest
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/update")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePostComment(@RequestBody PostCommentUpdateRequest postCommentUpdateRequest) {
		if (postCommentUpdateRequest == null || postCommentUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		PostComment postComment = new PostComment();
		BeanUtils.copyProperties(postCommentUpdateRequest, postComment);
		// 数据校验
		try {
			postCommentService.validPostComment(postComment, false);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// 判断是否存在
		long id = postCommentUpdateRequest.getId();
		PostComment oldPostComment = postCommentService.getById(id);
		ThrowUtils.throwIf(oldPostComment == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = postCommentService.updateById(postComment);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取帖子评论（封装类）
	 *
	 * @param id id
	 * @return {@link BaseResponse<PostCommentVO>}
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PostCommentVO> getPostCommentVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		PostComment postComment = postCommentService.getById(id);
		ThrowUtils.throwIf(postComment == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(postCommentService.getPostCommentVO(postComment, request));
	}
	
	/**
	 * 分页获取帖子评论列表（仅管理员可用）
	 *
	 * @param postCommentQueryRequest postCommentQueryRequest
	 * @return {@link BaseResponse<Page<PostComment>>}
	 */
	@PostMapping("/list/page")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<PostComment>> listPostCommentByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest) {
		long current = postCommentQueryRequest.getCurrent();
		long size = postCommentQueryRequest.getPageSize();
		// 查询数据库
		Page<PostComment> postCommentPage = postCommentService.page(new Page<>(current, size),
				postCommentService.getQueryWrapper(postCommentQueryRequest));
		return ResultUtils.success(postCommentPage);
	}
	
	/**
	 * 分页获取帖子评论列表（封装类）
	 *
	 * @param postCommentQueryRequest postCommentQueryRequest
	 * @param request                 request
	 * @return {@link BaseResponse<Page<PostCommentVO>>}
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PostCommentVO>> listPostCommentVOByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest,
	                                                                 HttpServletRequest request) {
		long current = postCommentQueryRequest.getCurrent();
		long size = postCommentQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<PostComment> postCommentPage = postCommentService.page(new Page<>(current, size),
				postCommentService.getQueryWrapper(postCommentQueryRequest));
		// 获取封装类
		return ResultUtils.success(postCommentService.getPostCommentVOPage(postCommentPage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的帖子评论列表
	 *
	 * @param postCommentQueryRequest postCommentQueryRequest
	 * @param request                 request
	 * @return {@link BaseResponse<Page<PostCommentVO>>}
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<PostCommentVO>> listMyPostCommentVOByPage(@RequestBody PostCommentQueryRequest postCommentQueryRequest,
	                                                                   HttpServletRequest request) {
		ThrowUtils.throwIf(postCommentQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		postCommentQueryRequest.setUserId(loginUser.getId());
		long current = postCommentQueryRequest.getCurrent();
		long size = postCommentQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<PostComment> postCommentPage = postCommentService.page(new Page<>(current, size),
				postCommentService.getQueryWrapper(postCommentQueryRequest));
		// 获取封装类
		return ResultUtils.success(postCommentService.getPostCommentVOPage(postCommentPage, request));
	}
	
	/**
	 * 编辑帖子评论（给用户使用）
	 *
	 * @param postCommentEditRequest postCommentEditRequest
	 * @param request                request
	 * @return {@link BaseResponse<Boolean>}
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPostComment(@RequestBody PostCommentEditRequest postCommentEditRequest, HttpServletRequest request) {
		if (postCommentEditRequest == null || postCommentEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		PostComment postComment = new PostComment();
		BeanUtils.copyProperties(postCommentEditRequest, postComment);
		// 数据校验
		postCommentService.validPostComment(postComment, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = postCommentEditRequest.getId();
		PostComment oldPostComment = postCommentService.getById(id);
		ThrowUtils.throwIf(oldPostComment == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldPostComment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = postCommentService.updateById(postComment);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
}