package com.stephen.trajectory.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.manager.ai.AIManager;
import com.stephen.trajectory.manager.redis.RedisLimiterManager;
import com.stephen.trajectory.model.dto.chart.*;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.chart.ChartStatusEnum;
import com.stephen.trajectory.model.enums.file.FileUploadBizEnum;
import com.stephen.trajectory.model.vo.ChartVO;
import com.stephen.trajectory.service.ChartService;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.document.excel.ExcelUtils;
import com.stephen.trajectory.utils.document.file.FileUtils;
import com.stephen.trajectory.utils.json.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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
	
	@Resource
	private AIManager aiManager;
	
	@Resource
	private RedisLimiterManager redisLimiterManager;
	
	@Resource
	private ThreadPoolExecutor threadPoolExecutor;


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
	public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
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
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
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
	@SaCheckRole(UserConstant.ADMIN_ROLE)
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
		Page<Chart> chartPage = chartService.page(new Page<>(current, size),
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
		// 查询数据库
		Page<Chart> chartPage = chartService.page(new Page<>(current, size),
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
	
	
	/**
	 * 通过生成图表(同步调用)
	 *
	 * @param multipartFile       上传的Excel文件
	 * @param genChartByAiRequest 生成图表请求对象
	 * @param request             HTTP请求对象
	 * @return {@link BaseResponse <{@link BIResponse}>}
	 */
	@PostMapping("/gen")
	public BaseResponse<BIResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
	                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
		
		// 验证用户和请求参数
		Chart chart = new Chart();
		BeanUtils.copyProperties(genChartByAiRequest, chart);
		String biz = genChartByAiRequest.getBiz();
		// 验证文件
		FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
		FileUtils.validFile(multipartFile, fileUploadBizEnum);
		// 数据校验
		chartService.validChart(chart, true);
		
		// 需要用户登录才能调用接口
		User loginUser = userService.getLoginUser(request);
		// 限流
		redisLimiterManager.doRateLimit("chart:gen:" + loginUser.getId());
		// 构造用户输入
		StringBuilder userInput = new StringBuilder();
		userInput.append("分析需求: ").append("\n");
		
		// 拼接分析目标
		String userGoal = chart.getGoal();
		if (StringUtils.isNotBlank(userGoal)) {
			userInput.append(userGoal)
					.append(",请使用")
					.append(chart.getChartType())
					.append("进行分析。\n");
		} else {
			userInput.append("无明确目标，生成通用分析图表。\n");
		}
		// 压缩之后的数据
		userInput.append("原始数据:\n");
		String excelToCsv = ExcelUtils.excelToCsv(multipartFile);
		if (StringUtils.isBlank(excelToCsv)) {
			log.error("文件转换为 CSV 数据失败");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件数据转换失败");
		}
		userInput.append(excelToCsv).append("\n");
		
		// todo 填充默认数据
		chart.setChartData(excelToCsv);
		chart.setStatus(ChartStatusEnum.WAIT.getValue());
		chart.setExecutorMessage(ChartStatusEnum.WAIT.getText());
		chart.setUserId(loginUser.getId());
		// 保存生成的图表数据
		boolean saveResult = chartService.save(chart);
		if (!saveResult) {
			executorError(chart.getId(), "数据更新失败");
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
		}
		// 返回生成的图表响应
		BIResponse biResponse = new BIResponse();
		biResponse.setChartId(chart.getId());
		// 调用 AI 服务生成配置
		String result = aiManager.doChat(userInput.toString());
		if (StringUtils.isBlank(result)) {
			executorError(chart.getId(), "AI 响应为空");
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 响应为空");
		}
		// 处理 AI 返回内容
		result = result.replaceAll("```json", "").replaceAll("```", "").trim();
		String[] split = result.split("'【【【【【'");
		if (split.length > 3) {
			executorError(chart.getId(), "AI 生成格式错误");
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 生成格式错误");
		}
		
		String genChart = split[1].trim();
		String genResult = split[2].trim();
		// 校验生成的 JSON 格式
		if (!JSONUtils.isValidJsonObject(genChart)) {
			executorError(chart.getId(), "AI 生成的图表配置格式错误");
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 生成的图表配置格式错误");
		}
		Chart updateChartResult = new Chart();
		updateChartResult.setId(chart.getId());
		updateChartResult.setGenChart(genChart);
		updateChartResult.setGenResult(genResult);
		updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());
		updateChartResult.setExecutorMessage(ChartStatusEnum.SUCCEED.getText());
		// 更新图表数据
		boolean updateResult = chartService.updateById(updateChartResult);
		if (!updateResult) {
			executorError(chart.getId(), "数据更新失败");
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
		}
		// 返回生成的图表响应
		biResponse.setGenChart(genChart);
		biResponse.setGenResult(genResult);
		return ResultUtils.success(biResponse);
	}
	
	/**
	 * 通过生成图表(异步调用)
	 *
	 * @param multipartFile       上传的Excel文件
	 * @param genChartByAiRequest 生成图表请求对象
	 * @param request             HTTP请求对象
	 * @return {@link BaseResponse <{@link BIResponse}>}
	 */
	@PostMapping("/gen/async")
	public BaseResponse<BIResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
	                                                  GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
		
		// 验证用户和请求参数
		Chart chart = new Chart();
		BeanUtils.copyProperties(genChartByAiRequest, chart);
		// 对数据进行校验
		String biz = genChartByAiRequest.getBiz();
		FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
		FileUtils.validFile(multipartFile, fileUploadBizEnum);
		chartService.validChart(chart, true);
		
		// 需要用户登录才能调用接口
		User loginUser = userService.getLoginUser(request);
		// 限流
		redisLimiterManager.doRateLimit("chart:gen:" + loginUser.getId());
		// 构造用户输入
		StringBuilder userInput = new StringBuilder();
		userInput.append("分析需求: ").append("\n");
		
		// 拼接分析目标
		String userGoal = chart.getGoal();
		if (StringUtils.isNotBlank(userGoal)) {
			userInput.append(userGoal)
					.append(",请使用")
					.append(chart.getChartType())
					.append("进行分析。\n");
		} else {
			userInput.append("无明确目标，生成通用分析图表。\n");
		}
		// 压缩之后的数据
		userInput.append("原始数据:\n");
		String excelToCsv = ExcelUtils.excelToCsv(multipartFile);
		if (StringUtils.isBlank(excelToCsv)) {
			log.error("文件转换为 CSV 数据失败");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件数据转换失败");
		}
		userInput.append(excelToCsv).append("\n");
		
		// todo 填充默认数据
		chart.setChartData(excelToCsv);
		chart.setStatus(ChartStatusEnum.WAIT.getValue());
		chart.setExecutorMessage(ChartStatusEnum.WAIT.getText());
		chart.setUserId(loginUser.getId());
		// 保存生成的图表数据
		boolean saveResult = chartService.save(chart);
		if (!saveResult) {
			executorError(chart.getId(), "数据更新失败");
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
		}
		// 返回生成的图表响应
		BIResponse biResponse = new BIResponse();
		biResponse.setChartId(chart.getId());
		// 使用异步化方法和自定义线程池调用 AI 服务
		try {
			CompletableFuture.runAsync(() -> {
				// 先将图表的执行状态为执行中
				Chart updateChart = new Chart();
				updateChart.setId(chart.getId());
				updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
				chart.setExecutorMessage(ChartStatusEnum.RUNNING.getText());
				boolean b = chartService.updateById(updateChart);
				if (!b) {
					executorError(chart.getId(), "数据更新失败");
					throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
				}
				// 调用 AI 服务生成配置
				String result = aiManager.doChat(userInput.toString());
				if (StringUtils.isBlank(result)) {
					throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应为空");
				}
				// 处理 AI 返回内容
				result = result.replaceAll("```json", "").replaceAll("```", "").trim();
				String[] split = result.split("'【【【【【'");
				if (split.length > 3) {
					executorError(chart.getId(), "AI 响应格式错误");
					throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 响应格式错误");
				}
				
				String genChart = split[1].trim();
				String genResult = split[2].trim();
				// 校验生成的 JSON 格式
				if (!JSONUtils.isValidJsonObject(genChart)) {
					executorError(chart.getId(), "AI 生成的图表配置格式错误");
					throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 生成的图表配置格式错误");
				}
				Chart updateChartResult = new Chart();
				updateChartResult.setId(chart.getId());
				updateChartResult.setGenChart(genChart);
				updateChartResult.setGenResult(genResult);
				updateChartResult.setStatus(ChartStatusEnum.SUCCEED.getValue());
				updateChartResult.setExecutorMessage(ChartStatusEnum.SUCCEED.getText());
				// 更新图表数据
				boolean updateResult = chartService.updateById(updateChartResult);
				if (!updateResult) {
					executorError(chart.getId(), "数据更新失败");
					throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
				}
				// 更新返回结果
				biResponse.setGenChart(genChart);
				biResponse.setGenResult(genResult);
			}, threadPoolExecutor);
		} catch (Exception e) {
			executorError(chart.getId(), "AI 服务调用失败" + e.getMessage());
		}
		return ResultUtils.success(biResponse);
	}
	
	/**
	 * AI 服务调用失败处理
	 *
	 * @param chartId         chartId
	 * @param executorMessage executorMessage
	 */
	private void executorError(Long chartId, String executorMessage) {
		log.error("AI 服务调用失败: {}", executorMessage);
		Chart chart = new Chart();
		chart.setId(chartId);
		chart.setStatus(ChartStatusEnum.FAILED.getValue());
		chart.setExecutorMessage(executorMessage);
		boolean b = chartService.updateById(chart);
		ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "数据更新失败");
	}
	
}