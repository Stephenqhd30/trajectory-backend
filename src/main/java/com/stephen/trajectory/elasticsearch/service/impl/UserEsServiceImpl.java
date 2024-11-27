package com.stephen.trajectory.elasticsearch.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.elasticsearch.modal.entity.UserEsDTO;
import com.stephen.trajectory.elasticsearch.service.UserEsService;
import com.stephen.trajectory.mapper.UserMapper;
import com.stephen.trajectory.model.dto.user.UserQueryRequest;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.user.UserGenderEnum;
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
 * UserEsService 实现类，负责从 Elasticsearch 中搜索用户并结合数据库获取动态数据。
 *
 * @author stephen qiu
 */
@Slf4j
@Service
public class UserEsServiceImpl implements UserEsService {
	
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;
	
	
	@Resource
	private UserMapper userMapper;
	
	/**
	 * 从 Elasticsearch 搜索用户，结合数据库信息返回完整数据。
	 *
	 * @param userQueryRequest 查询条件封装对象
	 * @return Page<User> 返回分页后的用户列表
	 */
	@Override
	public Page<User> searchUserFromEs(UserQueryRequest userQueryRequest) {
		// 获取查询条件
		BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(userQueryRequest);
		// 分页与排序参数
		// Elasticsearch 页码从 0 开始
		long current = userQueryRequest.getCurrent() - 1;
		long pageSize = userQueryRequest.getPageSize();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();
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
		SearchHits<UserEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, UserEsDTO.class);
		
		Page<User> page = new Page<>();
		page.setTotal(searchHits.getTotalHits());
		List<User> resourceList = new ArrayList<>();
		// 查出结果后，从 db 获取最新动态数据
		if (searchHits.hasSearchHits()) {
			List<SearchHit<UserEsDTO>> searchHitList = searchHits.getSearchHits();
			List<Long> userIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
					.collect(Collectors.toList());
			List<User> userList = userMapper.selectBatchIds(userIdList);
			
			if (userList != null) {
				Map<Long, User> idUserMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
				
				searchHitList.forEach(searchHit -> {
					Long userId = searchHit.getContent().getId();
					if (idUserMap.containsKey(userId)) {
						resourceList.add(idUserMap.get(userId));
					} else {
						// 异步删除无效的 ES 数据
						log.info("User with id {} does not exist in DB. Deleting from ES.", userId);
						elasticsearchRestTemplate.delete(String.valueOf(userId), UserEsDTO.class);
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
	 * @param userQueryRequest userQueryRequest
	 * @return BoolQueryBuilder 查询条件
	 */
	@Override
	public BoolQueryBuilder getBoolQueryBuilder(UserQueryRequest userQueryRequest) {
		// 从请求中获取查询参数
		Long id = userQueryRequest.getId();
		Long notId = userQueryRequest.getNotId();
		String userName = userQueryRequest.getUserName();
		Integer userGender = userQueryRequest.getUserGender();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		String userEmail = userQueryRequest.getUserEmail();
		String userPhone = userQueryRequest.getUserPhone();
		List<String> tagList = userQueryRequest.getTags();
		List<String> orTagList = userQueryRequest.getOrTags();
		String searchText = userQueryRequest.getSearchText();
		
		// 构建查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 1. 过滤已删除标记
		boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
		
		// 2. 处理 ID 筛选
		if (id != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
		}
		// 处理排除 ID 筛选
		if (notId != null) {
			boolQueryBuilder.filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("id", notId)));
		}
		
		// 3. 处理标签筛选
		// 必须包含所有标签
		if (CollUtil.isNotEmpty(tagList)) {
			tagList.forEach(tag -> boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag)));
		}
		
		// 包含任意一个标签即可
		if (CollUtil.isNotEmpty(orTagList)) {
			BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
			orTagList.forEach(tag -> orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag)));
			// 至少匹配一个标签
			orTagBoolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.filter(orTagBoolQueryBuilder);
		}
		
		// 4. 处理其它字段的精确查询（如用户角色、性别等）
		if (ObjectUtils.isNotEmpty(userGender) && UserGenderEnum.getEnumByValue(userGender) != null) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userGender", userGender));
		}
		if (StringUtils.isNotBlank(userProfile)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userProfile", userProfile));
		}
		if (StringUtils.isNotBlank(userRole)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userRole", userRole));
		}
		if (StringUtils.isNotBlank(userEmail)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userEmail", userEmail));
		}
		if (StringUtils.isNotBlank(userPhone)) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("userPhone", userPhone));
		}
		
		// 5. 处理全文搜索（按用户名称、标题或内容检索）
		if (StringUtils.isNotBlank(userName)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("userName", userName));
		}
		if (StringUtils.isNotBlank(userProfile)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("userProfile", userProfile));
		}
		
		// 全文搜索: 按标题、内容检索
		if (StringUtils.isNotBlank(searchText)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery("userName", userQueryRequest.getSearchText()));
			boolQueryBuilder.should(QueryBuilders.matchQuery("userProfile", userQueryRequest.getSearchText()));
			// 至少匹配一个字段
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
