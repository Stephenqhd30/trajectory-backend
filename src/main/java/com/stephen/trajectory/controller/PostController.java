package com.stephen.trajectory.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.annotation.AuthCheck;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.exception.BusinessException;
import com.stephen.trajectory.model.dto.post.PostAddRequest;
import com.stephen.trajectory.model.dto.post.PostEditRequest;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.dto.post.PostUpdateRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
	
	@Resource
	private PostService postService;
	
	@Resource
	private UserService userService;
	
	// region 增删改查
	
	/**
	 * 创建
	 *
	 * @param postAddRequest postAddRequest
	 * @param request        request
	 * @return BaseResponse<Long>
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
		if (postAddRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Post post = new Post();
		BeanUtils.copyProperties(postAddRequest, post);
		List<String> tags = postAddRequest.getTags();
		if (tags != null) {
			post.setTags(JSONUtil.toJsonStr(tags));
		}
		postService.validPost(post, true);
		User loginUser = userService.getLoginUser(request);
		post.setUserId(loginUser.getId());
		post.setFavourNum(0);
		post.setThumbNum(0);
		boolean result = postService.save(post);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		long newPostId = post.getId();
		return ResultUtils.success(newPostId);
	}
	
	/**
	 * 删除
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Post oldPost = postService.getById(id);
		ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldPost.getUserId().equals(user.getId()) && userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean b = postService.removeById(id);
		return ResultUtils.success(b);
	}
	
	/**
	 * 更新（仅管理员）
	 *
	 * @param postUpdateRequest postUpdateRequest
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
		ThrowUtils.throwIf(postUpdateRequest == null || postUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
		Post post = new Post();
		BeanUtils.copyProperties(postUpdateRequest, post);
		List<String> tags = postUpdateRequest.getTags();
		if (tags != null) {
			post.setTags(JSONUtil.toJsonStr(tags));
		}
		// 参数校验
		postService.validPost(post, false);
		long id = postUpdateRequest.getId();
		// 判断是否存在
		Post oldPost = postService.getById(id);
		ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
		boolean result = postService.updateById(post);
		return ResultUtils.success(result);
	}
	
	/**
	 * 根据 id 获取
	 *
	 * @param id id
	 * @return BaseResponse<PostVO>
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PostVO> getPostVOById(long id, HttpServletRequest request) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Post post = postService.getById(id);
		if (post == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		return ResultUtils.success(postService.getPostVO(post, request));
	}
	
	/**
	 * 分页获取列表（仅管理员）
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return BaseResponse<Page < Post>>
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
		long current = postQueryRequest.getCurrent();
		long size = postQueryRequest.getPageSize();
		Page<Post> postPage = postService.page(new Page<>(current, size),
				postService.getQueryWrapper(postQueryRequest));
		return ResultUtils.success(postPage);
	}
	
	/**
	 * 分页获取列表（封装类）
	 *
	 * @param postQueryRequest postQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
	                                                   HttpServletRequest request) {
		long current = postQueryRequest.getCurrent();
		long size = postQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postService.page(new Page<>(current, size),
				postService.getQueryWrapper(postQueryRequest));
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
	/**
	 * 分页获取当前用户创建的资源列表
	 *
	 * @param postQueryRequest postQueryRequest
	 * @param request          request
	 * @return
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
	                                                     HttpServletRequest request) {
		ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		postQueryRequest.setUserId(loginUser.getId());
		long current = postQueryRequest.getCurrent();
		long size = postQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postService.page(new Page<>(current, size),
				postService.getQueryWrapper(postQueryRequest));
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
	// endregion
	
	/**
	 * 编辑（用户）
	 *
	 * @param postEditRequest postEditRequest
	 * @param request         request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest postEditRequest, HttpServletRequest request) {
		if (postEditRequest == null || postEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Post post = new Post();
		BeanUtils.copyProperties(postEditRequest, post);
		List<String> tags = postEditRequest.getTags();
		if (tags != null) {
			post.setTags(JSONUtil.toJsonStr(tags));
		}
		// 参数校验
		postService.validPost(post, false);
		User loginUser = userService.getLoginUser(request);
		long id = postEditRequest.getId();
		// 判断是否存在
		Post oldPost = postService.getById(id);
		ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldPost.getUserId().equals(loginUser.getId()) && userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean result = postService.updateById(post);
		return ResultUtils.success(result);
	}
}