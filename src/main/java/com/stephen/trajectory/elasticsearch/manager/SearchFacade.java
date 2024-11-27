package com.stephen.trajectory.elasticsearch.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.datasources.DataSource;
import com.stephen.trajectory.elasticsearch.datasources.DataSourceRegistry;
import com.stephen.trajectory.elasticsearch.datasources.PostDataSource;
import com.stephen.trajectory.elasticsearch.datasources.UserDataSource;
import com.stephen.trajectory.elasticsearch.modal.dto.SearchRequest;
import com.stephen.trajectory.elasticsearch.modal.enums.SearchTypeEnum;
import com.stephen.trajectory.elasticsearch.modal.vo.SearchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: stephen qiu
 * @create: 2024-06-13 16:01
 **/
@Component
@Slf4j
public class SearchFacade {
	
	@Resource
	private PostDataSource postDataSource;
	
	@Resource
	private UserDataSource userDataSource;
	
	@Resource
	private DataSourceRegistry dataSourceRegistry;
	
	/**
	 * 聚合搜索查询
	 *
	 * @param searchRequest searchRequest
	 * @return SearchVO
	 */
	public SearchVO<Object> searchAll(@RequestBody SearchRequest searchRequest, HttpServletRequest request) {
		// 获取查询类型，若为空则使用默认值
		String type = Optional.ofNullable(searchRequest.getType())
				.orElse(SearchTypeEnum.POST.getValue());
		// 获取对应数据源
		DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
		// 执行查询
		Page<?> page = dataSource.doSearch(searchRequest, request);
		// 将查询结果放入 dataList
		List<Object> dataList = new ArrayList<>(page.getRecords());
		// 返回搜索结果
		SearchVO<Object> searchVO = new SearchVO<>();
		searchVO.setDataList(dataList);
		return searchVO;
	}
}