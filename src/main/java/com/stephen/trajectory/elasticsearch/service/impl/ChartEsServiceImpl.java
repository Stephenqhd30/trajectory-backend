package com.stephen.trajectory.elasticsearch.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.elasticsearch.modal.entity.ChartEsDTO;
import com.stephen.trajectory.elasticsearch.service.ChartEsService;
import com.stephen.trajectory.mapper.ChartMapper;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.enums.chart.ChartStatusEnum;
import com.stephen.trajectory.model.enums.chart.ChartTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ChartEsService 实现类，
 * 负责从 Elasticsearch 中搜索用户并结合数据库获取动态数据。
 *
 * @author stephen qiu
 */
@Slf4j
@Service
public class ChartEsServiceImpl implements ChartEsService {
	
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;
	
	@Resource
	private ChartMapper chartMapper;
	
	/**
	 * 根据查询条件从 Elasticsearch 中搜索用户并结合数据库获取动态数据。
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link Page < {@link Chart}>}
	 */
	@Override
	public Page<Chart> searchChartFromEs(ChartQueryRequest chartQueryRequest) {
		// 获取查询条件
		BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(chartQueryRequest);
		// 分页与排序参数
		PageRequest pageRequest = PageRequest.of(chartQueryRequest.getCurrent() - 1, chartQueryRequest.getPageSize());
		SortBuilder<?> sortBuilder = this.getSortBuilder(chartQueryRequest.getSortField(), chartQueryRequest.getSortOrder());
		
		// 执行查询
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.withPageable(pageRequest)
				.withSorts(sortBuilder)
				.build();
		SearchHits<ChartEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, ChartEsDTO.class);
		
		Page<Chart> page = new Page<>();
		page.setTotal(searchHits.getTotalHits());
		List<Chart> resourceList = new ArrayList<>();
		
		if (searchHits.hasSearchHits()) {
			List<Long> chartIdList = searchHits.getSearchHits().stream()
					.map(searchHit -> searchHit.getContent().getId())
					.collect(Collectors.toList());
			List<Chart> chartList = chartMapper.selectBatchIds(chartIdList);
			
			if (chartList != null) {
				Map<Long, Chart> idChartMap = chartList.stream()
						.collect(Collectors.toMap(Chart::getId, chart -> chart));
				
				searchHits.getSearchHits().forEach(searchHit -> {
					Long chartId = searchHit.getContent().getId();
					Chart chart = idChartMap.get(chartId);
					if (chart != null) {
						resourceList.add(chart);
					} else {
						// 异步删除无效的 ES 数据
						CompletableFuture.runAsync(() -> {
							log.info("Chart with id {} does not exist in DB. Deleting from ES.", chartId);
							elasticsearchRestTemplate.delete(String.valueOf(chartId), ChartEsDTO.class);
						});
					}
				});
			}
		}
		
		page.setRecords(resourceList);
		return page;
	}
	
	@Override
	public BoolQueryBuilder getBoolQueryBuilder(ChartQueryRequest chartQueryRequest) {
		// 从请求中获取查询参数
		Long id = chartQueryRequest.getId();
		Long notId = chartQueryRequest.getNotId();
		String searchText = chartQueryRequest.getSearchText();
		String goal = chartQueryRequest.getGoal();
		String name = chartQueryRequest.getName();
		String chartType = chartQueryRequest.getChartType();
		String executorMessage = chartQueryRequest.getExecutorMessage();
		Long userId = chartQueryRequest.getUserId();
		
		// 构建查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 1. 过滤已删除标记并只查询生成成功的图表
		boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
		boolQueryBuilder.filter(QueryBuilders.termQuery("status", ChartStatusEnum.SUCCEED.getValue()));
		
		// 2. 处理 ID 筛选
		if (id != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
		}
		if (notId != null) {
			boolQueryBuilder.filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("id", notId)));
		}
		
		// 3. 处理其它字段的精确查询（如用户角色、性别等）
		if (ObjectUtils.isNotEmpty(chartType) && ChartTypeEnum.getEnumByValue(chartType) != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("chartType", chartType));
		}
		if (ObjectUtils.isNotEmpty(userId)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
		}
		
		// 5. 处理全文搜索（按用户名称、标题或内容检索）
		if (StringUtils.isNotBlank(executorMessage)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("executorMessage", executorMessage));
		}
		if (StringUtils.isNotBlank(goal)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("goal", goal));
		}
		if (StringUtils.isNotBlank(name)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("name", name));
		}
		
		// 全文搜索: 按标题、内容检索
		if (StringUtils.isNotBlank(searchText)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("goal", searchText));
			boolQueryBuilder.should(QueryBuilders.matchQuery("name", searchText));
			boolQueryBuilder.minimumShouldMatch(1);
		} else {
			// 如果没有提供 searchText，构建一个默认查询条件（匹配所有记录）
			boolQueryBuilder.must(QueryBuilders.matchAllQuery());
		}
		
		return boolQueryBuilder;
	}
	
	@Override
	public SortBuilder<?> getSortBuilder(String sortField, String sortOrder) {
		// 默认按相关度排序
		SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
		if (StringUtils.isNotBlank(sortField)) {
			sortBuilder = SortBuilders.fieldSort(sortField)
					.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
		}
		return sortBuilder;
	}
}