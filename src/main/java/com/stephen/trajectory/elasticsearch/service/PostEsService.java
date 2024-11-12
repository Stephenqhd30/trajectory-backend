package com.stephen.trajectory.elasticsearch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.entity.PostEsDTO;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

/**
 * @author: stephen qiu
 * @create: 2024-11-05 14:35
 **/
public interface PostEsService {
	
	/**
	 * 从ES中搜索文章
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return {@link Page}<{@link PostEsDTO}>
	 */
	Page<Post> searchPostFromEs(PostQueryRequest postQueryRequest);
	
	/**
	 * 构建查询条件
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return {@link BoolQueryBuilder}
	 */
	BoolQueryBuilder getBoolQueryBuilder(PostQueryRequest postQueryRequest);
	
	/**
	 * 构建排序条件
	 *
	 * @param sortField 排序字段
	 * @param sortOrder 排序顺序
	 * @return SortBuilder 排序构建器
	 */
	SortBuilder<?> getSortBuilder(String sortField, String sortOrder);
}
