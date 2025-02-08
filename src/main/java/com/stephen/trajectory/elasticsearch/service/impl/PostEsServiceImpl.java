package com.stephen.trajectory.elasticsearch.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 帖子 ES 服务实现类
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
	 * 从ES中搜索
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return Page<Post>
	 */
	@Override
	public Page<Post> searchPostFromEs(PostQueryRequest postQueryRequest) {
		BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(postQueryRequest);
		PageRequest pageRequest = PageRequest.of(postQueryRequest.getCurrent() - 1, postQueryRequest.getPageSize());
		SortBuilder<?> sortBuilder = getSortBuilder(postQueryRequest.getSortField(), postQueryRequest.getSortOrder());
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.withPageable(pageRequest)
				.withSorts(sortBuilder)
				.build();
		// 执行 Elasticsearch 查询
		SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
		List<Post> resourceList = new ArrayList<>();
		
		if (searchHits.hasSearchHits()) {
			List<Long> postIdList = searchHits.getSearchHits().stream()
					.map(searchHit -> searchHit.getContent().getId())
					.collect(Collectors.toList());
			
			// 查询数据库获取文章详细信息
			List<Post> postList = postMapper.selectBatchIds(postIdList);
			
			// 生成一个 id 到 Post 对象的映射
			Map<Long, Post> idPostMap = postList.stream().collect(Collectors.toMap(Post::getId, post -> post));
			
			// 遍历搜索结果，构建最终的帖子列表
			searchHits.getSearchHits().forEach(searchHit -> {
				Long postId = searchHit.getContent().getId();
				if (idPostMap.containsKey(postId)) {
					resourceList.add(idPostMap.get(postId));
				} else {
					// 异步删除无效的 ES 数据
					log.info("Post with id {} does not exist in DB. Deleting from ES.", postId);
					CompletableFuture.runAsync(() -> elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class));
				}
			});
		}
		
		// 创建分页结果
		Page<Post> page = new Page<>();
		page.setTotal(searchHits.getTotalHits());
		page.setRecords(resourceList);
		return page;
	}
	
	/**
	 * 构建 Elasticsearch 查询条件
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return {@link BoolQueryBuilder}
	 */
	@Override
	public BoolQueryBuilder getBoolQueryBuilder(PostQueryRequest postQueryRequest) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 删除标识
		boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
		
		// 如果没有提供 searchText，构建一个默认查询条件（可以修改为符合需求的条件）
		if (StringUtils.isNotBlank(postQueryRequest.getSearchText())) {
			boolQueryBuilder.should(QueryBuilders.multiMatchQuery(postQueryRequest.getSearchText(), "title", "description", "content"));
			// 至少匹配一个字段
			boolQueryBuilder.minimumShouldMatch(1);
		} else {
			// 默认搜索全部（例如匹配所有帖子）
			boolQueryBuilder.must(QueryBuilders.matchAllQuery());
		}
		
		if (postQueryRequest.getId() != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("id", postQueryRequest.getId()));
		}
		
		if (CollUtil.isNotEmpty(postQueryRequest.getTags())) {
			postQueryRequest.getTags().forEach(tag -> boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag)));
		}
		
		// 更多条件可以根据需求继续增加
		return boolQueryBuilder;
	}
	
	/**
	 * 构建排序条件
	 *
	 * @param sortField 排序字段
	 * @param sortOrder 排序顺序
	 * @return {@link SortBuilder  <{@link ?}>}
	 */
	@Override
	public SortBuilder<?> getSortBuilder(String sortField, String sortOrder) {
		SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
		if (StringUtils.isNotBlank(sortField)) {
			sortBuilder = SortBuilders.fieldSort(sortField)
					.order("asc".equalsIgnoreCase(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
		}
		return sortBuilder;
	}
}
