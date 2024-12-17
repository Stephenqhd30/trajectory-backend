package com.stephen.trajectory.elasticsearch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.modal.entity.ChartEsDTO;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

/**
 * 图表ES服务
 *
 * @author: stephen qiu
 **/
public interface ChartEsService {
	
	/**
	 * 从ES中搜索文章
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link Page}<{@link ChartEsDTO}>
	 */
	Page<Chart> searchChartFromEs(ChartQueryRequest chartQueryRequest);
	
	/**
	 * 构建查询条件
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link BoolQueryBuilder}
	 */
	BoolQueryBuilder getBoolQueryBuilder(ChartQueryRequest chartQueryRequest);
	
	/**
	 * 构建排序条件
	 *
	 * @param sortField 排序字段
	 * @param sortOrder 排序顺序
	 * @return SortBuilder 排序构建器
	 */
	SortBuilder<?> getSortBuilder(String sortField, String sortOrder);
}
