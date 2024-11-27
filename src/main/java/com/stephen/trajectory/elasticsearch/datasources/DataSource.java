package com.stephen.trajectory.elasticsearch.datasources;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.modal.dto.SearchRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 数据源接口（需要接入的数据源必须实现）
 *
 * @author: stephen qiu
 **/
public interface DataSource<T> {
	
	/**
	 * 搜索
	 *
	 * @param searchRequest 搜索条件
	 * @param request       request
	 * @return {@link Page<T>}
	 */
	Page<T> doSearch(SearchRequest searchRequest, HttpServletRequest request);
}
