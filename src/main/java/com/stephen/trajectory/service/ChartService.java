package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.vo.ChartVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 图表信息服务
 *
 * @author stephen qiu
 */
public interface ChartService extends IService<Chart> {
	
	/**
	 * 校验数据
	 *
	 * @param chart chart
	 * @param add   对创建的数据进行校验
	 */
	void validChart(Chart chart, boolean add);
	
	/**
	 * 获取查询条件
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link QueryWrapper<Chart>}
	 */
	QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
	
	/**
	 * 获取图表信息封装
	 *
	 * @param chart   chart
	 * @param request request
	 * @return {@link ChartVO}
	 */
	ChartVO getChartVO(Chart chart, HttpServletRequest request);
	
	/**
	 * 分页获取图表信息封装
	 *
	 * @param chartPage chartPage
	 * @param request   request
	 * @return {@link Page
	 * <ChartVO>}
	 */
	Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request);
}