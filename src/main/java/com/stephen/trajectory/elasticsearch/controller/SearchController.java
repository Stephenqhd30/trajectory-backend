package com.stephen.trajectory.elasticsearch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.elasticsearch.manager.SearchFacade;
import com.stephen.trajectory.elasticsearch.modal.dto.SearchRequest;
import com.stephen.trajectory.elasticsearch.modal.vo.SearchVO;
import com.stephen.trajectory.elasticsearch.service.ChartEsService;
import com.stephen.trajectory.elasticsearch.service.PostEsService;
import com.stephen.trajectory.elasticsearch.service.UserEsService;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.dto.user.UserQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.chart.ChartStatusEnum;
import com.stephen.trajectory.model.vo.ChartVO;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.ChartService;
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
public class SearchController {
	
	@Resource
	private SearchFacade searchFacade;
	
	@Resource
	private PostEsService postEsService;
	
	@Resource
	private UserEsService userEsService;
	
	@Resource
	private ChartEsService chartEsService;
	
	@Resource
	private PostService postService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private ChartService chartService;
	
	
	/**
	 * 使用门面模式进行重构
	 * 聚合搜索查询
	 *
	 * @param searchRequest searchRequest
	 * @return {@link BaseResponse <{@link SearchVO } <{@link Object}>>}
	 */
	@PostMapping("/all")
	public BaseResponse<SearchVO<Object>> doSearchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
		return ResultUtils.success(searchFacade.searchAll(searchRequest, request));
	}
	
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
	
	/**
	 * 分页搜索图表（从 ES 查询，封装类）
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @param request           request
	 * @return <{@link BaseResponse} <{@link Page } < {@link ChartVO }>>
	 */
	@PostMapping("/search/chart/page/vo")
	public BaseResponse<Page<ChartVO>> searchChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
	                                                       HttpServletRequest request) {
		long size = chartQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 仅查询生成成功的图表
		chartQueryRequest.setStatus(ChartStatusEnum.SUCCEED.getValue());
		Page<Chart> chartPage = chartEsService.searchChartFromEs(chartQueryRequest);
		return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
	}
}
