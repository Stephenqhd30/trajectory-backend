package com.stephen.trajectory.elasticsearch.datasources;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.annotation.DataSourceType;
import com.stephen.trajectory.elasticsearch.modal.dto.SearchRequest;
import com.stephen.trajectory.elasticsearch.modal.enums.SearchTypeEnum;
import com.stephen.trajectory.elasticsearch.service.ChartEsService;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.vo.ChartVO;
import com.stephen.trajectory.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务实现
 *
 * @author stephen qiu
 */
@DataSourceType(SearchTypeEnum.CHART)
@Component
@Slf4j
public class ChartDataSource implements DataSource<ChartVO> {
	
	@Resource
	private ChartService chartService;
	
	@Resource
	private ChartEsService chartEsService;
	
	/**
	 * 从ES中搜索用户
	 *
	 * @param searchRequest 搜索条件
	 * @param request       request
	 * @return {@link Page {@link ChartVO}}
	 */
	@Override
	public Page<ChartVO> doSearch(SearchRequest searchRequest, HttpServletRequest request) {
		ChartQueryRequest chartQueryRequest = new ChartQueryRequest();
		BeanUtils.copyProperties(searchRequest, chartQueryRequest);
		Page<Chart> chartPage = chartEsService.searchChartFromEs(chartQueryRequest);
		return chartService.getChartVOPage(chartPage, request);
	}
}