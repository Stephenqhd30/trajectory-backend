package com.stephen.trajectory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.mapper.ChartMapper;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.chart.ChartStatusEnum;
import com.stephen.trajectory.model.enums.chart.ChartTypeEnum;
import com.stephen.trajectory.model.vo.ChartVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.ChartService;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 图表信息服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {
	
	@Resource
	private UserService userService;
	
	/**
	 * 校验数据
	 *
	 * @param chart chart
	 * @param add   对创建的数据进行校验
	 */
	@Override
	public void validChart(Chart chart, boolean add) {
		ThrowUtils.throwIf(chart == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String goal = chart.getGoal();
		String name = chart.getName();
		String chartData = chart.getChartData();
		String chartType = chart.getChartType();
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, name, chartType), ErrorCode.PARAMS_ERROR, "参数不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(goal) && goal.length() > 80) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "分析目标内容过程");
		}
		if (StringUtils.isNotBlank(name) && name.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表名称过长");
		}
		if (StringUtils.isNotBlank(chartType)) {
			ThrowUtils.throwIf(ChartTypeEnum.getEnumByValue(chartType) == null, ErrorCode.PARAMS_ERROR, "图表类型错误");
		}
		if (StringUtils.isNotBlank(chartData) && chartData.length() > 8192) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link QueryWrapper<Chart>}
	 */
	@Override
	public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
		QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
		if (chartQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = chartQueryRequest.getId();
		Long notId = chartQueryRequest.getNotId();
		String goal = chartQueryRequest.getGoal();
		String name = chartQueryRequest.getName();
		String chartType = chartQueryRequest.getChartType();
		String status = chartQueryRequest.getStatus();
		String executorMessage = chartQueryRequest.getExecutorMessage();
		String searchText = chartQueryRequest.getSearchText();
		Long userId = chartQueryRequest.getUserId();
		String sortField = chartQueryRequest.getSortField();
		String sortOrder = chartQueryRequest.getSortOrder();
		
		// todo 补充需要的查询条件
		// 拼接查询条件
		if (StringUtils.isNotBlank(searchText)) {
			queryWrapper.and(qw -> qw.like("goal", searchText).or().like("name", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
		queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
		queryWrapper.like(StringUtils.isNotBlank(executorMessage), "executorMessage", executorMessage);
		// 精确查询
		queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
		queryWrapper.eq(StringUtils.isNotBlank(status), "status", status);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取图表信息封装
	 *
	 * @param chart   chart
	 * @param request request
	 * @return {@link ChartVO}
	 */
	@Override
	public ChartVO getChartVO(Chart chart, HttpServletRequest request) {
		// 对象转封装类
		ChartVO chartVO = ChartVO.objToVo(chart);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = chart.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		chartVO.setUserVO(userVO);
		// endregion
		return chartVO;
	}
	
	/**
	 * 分页获取图表信息封装
	 *
	 * @param chartPage chartPage
	 * @param request   request
	 * @return {@link Page<ChartVO>}
	 */
	@Override
	public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
		List<Chart> chartList = chartPage.getRecords();
		Page<ChartVO> chartVOPage = new Page<>(chartPage.getCurrent(), chartPage.getSize(), chartPage.getTotal());
		if (CollUtil.isEmpty(chartList)) {
			return chartVOPage;
		}
		// 对象列表 => 封装对象列表
		List<ChartVO> chartVOList = chartList.stream()
				.map(ChartVO::objToVo)
				.collect(Collectors.toList());
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = chartList.stream().map(Chart
				::getUserId).collect(Collectors.toSet());
		// 填充信息
		if (CollUtil.isNotEmpty(userIdSet)) {
			CompletableFuture<Map<Long, List<User>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId)));
			try {
				Map<Long, List<User>> userIdUserListMap = mapCompletableFuture.get();
				// 填充信息
				chartVOList.forEach(chartVO -> {
					Long userId = chartVO.getUserId();
					User user = null;
					if (userIdUserListMap.containsKey(userId)) {
						user = userIdUserListMap.get(userId).get(0);
					}
					chartVO.setUserVO(userService.getUserVO(user, request));
				});
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取信息失败" + e.getMessage());
			}
		}
		// endregion
		chartVOPage.setRecords(chartVOList);
		return chartVOPage;
	}
	
}