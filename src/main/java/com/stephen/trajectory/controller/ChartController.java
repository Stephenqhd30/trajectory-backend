package com.stephen.trajectory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.annotation.AuthCheck;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.exception.BusinessException;
import com.stephen.trajectory.model.dto.chart.ChartAddRequest;
import com.stephen.trajectory.model.dto.chart.ChartEditRequest;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.dto.chart.ChartUpdateRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.ChartVO;
import com.stephen.trajectory.service.ChartService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图表信息接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {
	
	@Resource
	private ChartService chartService;
	
	@Resource
	private UserService userService;

// region 增删改查
	
	/**
	 * 创建图表信息
	 *
	 * @param chartAddRequest chartAddRequest
	 * @param request         request
	 * @return {@link BaseResponse
	 * <Long>}
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(chartAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		Chart chart = new Chart();
		BeanUtils.copyProperties(chartAddRequest, chart);
		// 数据校验
		try {
			chartService.validChart(chart, true);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		chart.setUserId(loginUser.getId());
		// 写入数据库
		boolean result = chartService.save(chart);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newChartId = chart.getId();
		return ResultUtils.success(newChartId);
	}
	
	/**
	 * 删除图表信息
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return {@link BaseResponse
	 * <Boolean>}
	 */
	@PostMapping("/delete")
	public BaseResponse
			<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Chart oldChart = chartService.getById(id);
		ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = chartService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新图表信息（仅管理员可用）
	 *
	 * @param chartUpdateRequest chartUpdateRequest
	 * @return {@link BaseResponse
	 * <Boolean>}
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse
			<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
		if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Chart chart = new Chart();
		BeanUtils.copyProperties(chartUpdateRequest, chart);
		// 数据校验
		try {
			chartService.validChart(chart, false);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// 判断是否存在
		long id = chartUpdateRequest.getId();
		Chart oldChart = chartService.getById(id);
		ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = chartService.updateById(chart);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取图表信息（封装类）
	 *
	 * @param id id
	 * @return {@link BaseResponse
	 * <ChartVO>}
	 */
	@GetMapping("/get/vo")
	public BaseResponse<ChartVO> getChartVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Chart chart = chartService.getById(id);
		ThrowUtils.throwIf(chart == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(chartService.getChartVO(chart, request));
	}
	
	/**
	 * 分页获取图表信息列表（仅管理员可用）
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link BaseResponse
	 * <Page
	 * <Chart>>}
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
		long current = chartQueryRequest.getCurrent();
		long size = chartQueryRequest.getPageSize();
		// 查询数据库
		Page<Chart> chartPage = chartService.page(new Page<>(current, size),
				chartService.getQueryWrapper(chartQueryRequest));
		return ResultUtils.success(chartPage);
	}
	
	/**
	 * 分页获取图表信息列表（封装类）
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @param request           request
	 * @return {@link BaseResponse
	 * <Page
	 * <ChartVO>>}
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<ChartVO>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
	                                                     HttpServletRequest request) {
		long current = chartQueryRequest.getCurrent();
		long size = chartQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Chart> chartPage = chartService.page(new
						Page<>(current, size),
				chartService.getQueryWrapper(chartQueryRequest));
		// 获取封装类
		return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的图表信息列表
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @param request           request
	 * @return {@link BaseResponse
	 * <Page
	 * <ChartVO>>}
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<ChartVO>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
	                                                       HttpServletRequest request) {
		ThrowUtils.throwIf(chartQueryRequest == null,
				ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		chartQueryRequest.setUserId(loginUser.getId());
		long current = chartQueryRequest.getCurrent();
		long size = chartQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Chart> chartPage = chartService.page(new
						Page<>(current, size),
				chartService.getQueryWrapper(chartQueryRequest));
		// 获取封装类
		return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
	}
	
	/**
	 * 编辑图表信息（给用户使用）
	 *
	 * @param chartEditRequest chartEditRequest
	 * @param request          request
	 * @return {@link BaseResponse
	 * <Boolean>}
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
		if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Chart chart = new Chart();
		BeanUtils.copyProperties(chartEditRequest, chart);
		// 数据校验
		chartService.validChart(chart, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = chartEditRequest.getId();
		Chart oldChart = chartService.getById(id);
		ThrowUtils.throwIf(oldChart == null,
				ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldChart.getUserId().equals(loginUser.getId()) &&
				!userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = chartService.updateById(chart);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
}