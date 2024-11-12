package com.stephen.trajectory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.elasticsearch.service.PostEsService;
import com.stephen.trajectory.elasticsearch.service.UserEsService;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.dto.user.UserQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 从ES执行搜索接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/es")
@Slf4j
public class ElasticsearchController {
	
	@Resource
	private PostEsService postEsService;
	
	@Resource
	private UserEsService userEsService;
	
	@Resource
	private PostService postService;
	
	@Resource
	private UserService userService;
	
	/**
	 * 分页搜索帖子（从 ES 查询，封装类）
	 *
	 * @param postQueryRequest postQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/search/post/page/vo")
	public BaseResponse<Page<PostVO>> searchPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
	                                                     HttpServletRequest request) {
		long size = postQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<Post> postPage = postEsService.searchPostFromEs(postQueryRequest);
		return ResultUtils.success(postService.getPostVOPage(postPage, request));
	}
	
	/**
	 * 分页搜索用户（从 ES 查询，封装类）
	 *
	 * @param userQueryRequest userQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < PostVO>>
	 */
	@PostMapping("/search/user/page/vo")
	public BaseResponse<Page<UserVO>> searchUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                                     HttpServletRequest request) {
		long size = userQueryRequest.getPageSize();
		int current = userQueryRequest.getCurrent();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<User> userPage = userEsService.searchUserFromEs(userQueryRequest);
		Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
		List<UserVO> userVO = userService.getUserVO(userPage.getRecords(), request);
		userVOPage.setRecords(userVO);
		return ResultUtils.success(userVOPage);
	}
}
