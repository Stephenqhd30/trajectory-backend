package com.stephen.trajectory.elasticsearch.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.elasticsearch.modal.entity.PostEsDTO;
import com.stephen.trajectory.elasticsearch.service.PostEsService;
import com.stephen.trajectory.mapper.PostMapper;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PostEsService 实现类，负责从 Elasticsearch 中搜索文章并结合数据库获取动态数据。
 *
 * @author stephen qiu
 */
@Slf4j
@Service
public class PostEsServiceImpl implements PostEsService {
	
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;
	
	@Resource
	private PostMapper postMapper;
	
	/**
	 * 从 Elasticsearch 搜索文章，结合数据库信息返回完整数据。
	 *
	 * @param postQueryRequest 查询条件封装对象
	 * @return Page<Post> 返回分页后的文章列表
	 */
	@Override
	public Page<Post> searchPostFromEs(PostQueryRequest postQueryRequest) {
		// 获取查询条件
		BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(postQueryRequest);
		// 分页与排序参数
		// Elasticsearch 页码从 0 开始
		long current = postQueryRequest.getCurrent() - 1;
		long pageSize = postQueryRequest.getPageSize();
		String sortField = postQueryRequest.getSortField();
		String sortOrder = postQueryRequest.getSortOrder();
		// 构建排序条件
		SortBuilder<?> sortBuilder = this.getSortBuilder(sortField, sortOrder);
		// 构建分页请求
		PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
		
		// 执行查询
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.withPageable(pageRequest)
				.withSorts(sortBuilder)
				.build();
		SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
		
		Page<Post> page = new Page<>();
		page.setTotal(searchHits.getTotalHits());
		List<Post> resourceList = new ArrayList<>();
		// 查出结果后，从 db 获取最新动态数据（比如点赞数）
		if (searchHits.hasSearchHits()) {
			List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
			List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
					.collect(Collectors.toList());
			List<Post> postList = postMapper.selectBatchIds(postIdList);
			
			if (postList != null) {
				Map<Long, Post> idPostMap = postList.stream().collect(Collectors.toMap(Post::getId, post -> post));
				
				searchHitList.forEach(searchHit -> {
					Long postId = searchHit.getContent().getId();
					if (idPostMap.containsKey(postId)) {
						resourceList.add(idPostMap.get(postId));
					} else {
						// 异步删除无效的 ES 数据
						log.info("Post with id {} does not exist in DB. Deleting from ES.", postId);
						elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
					}
				});
			}
		}
		page.setRecords(resourceList);
		return page;
	}
	
	/**
	 * 构建 Elasticsearch 查询条件
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return BoolQueryBuilder 查询条件
	 */
	@Override
	public BoolQueryBuilder getBoolQueryBuilder(PostQueryRequest postQueryRequest) {
		// 1.从请求中获取查询参数
		Long id = postQueryRequest.getId();
		Long notId = postQueryRequest.getNotId();
		String searchText = postQueryRequest.getSearchText();
		String title = postQueryRequest.getTitle();
		String content = postQueryRequest.getContent();
		List<String> tagList = postQueryRequest.getTags();
		List<String> orTagList = postQueryRequest.getOrTags();
		Long userId = postQueryRequest.getUserId();
		// 2.构建查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 3.过滤
		boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
		// 处理 ID 筛选
		if (id != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
		}
		if (notId != null) {
			boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
		}
		// 处理用户 ID 筛选
		if (userId != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
		}
		// 4.处理标签筛选
		// 必须包含所有标签
		if (CollUtil.isNotEmpty(tagList)) {
			tagList.forEach(tag -> boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag)));
		}
		// 包含任何一个标签即可
		if (CollUtil.isNotEmpty(orTagList)) {
			BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
			orTagList.forEach(tag -> orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag)));
			// 至少匹配一个标签
			orTagBoolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.filter(orTagBoolQueryBuilder);
		}
		// 4.处理全文搜索
		// 按关键词检索
		if (StringUtils.isNotBlank(searchText)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
			boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
			boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
			// 至少匹配一个字段
			boolQueryBuilder.minimumShouldMatch(1);
		}
		// 按标题检索
		if (StringUtils.isNotBlank(title)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
			boolQueryBuilder.minimumShouldMatch(1);
		}
		// 按内容检索
		if (StringUtils.isNotBlank(content)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
			boolQueryBuilder.minimumShouldMatch(1);
		}
		return boolQueryBuilder;
	}
	
	/**
	 * 构建排序条件
	 *
	 * @param sortField 排序字段
	 * @param sortOrder 排序顺序
	 * @return SortBuilder 排序构建器
	 */
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
