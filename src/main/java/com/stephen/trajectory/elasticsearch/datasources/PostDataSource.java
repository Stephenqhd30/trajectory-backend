package com.stephen.trajectory.elasticsearch.datasources;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.elasticsearch.annotation.DataSourceType;
import com.stephen.trajectory.elasticsearch.modal.dto.SearchRequest;
import com.stephen.trajectory.elasticsearch.modal.enums.SearchTypeEnum;
import com.stephen.trajectory.elasticsearch.service.PostEsService;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务实现
 *
 * @author stephen qiu
 */

@DataSourceType(SearchTypeEnum.POST)
@Component
@Slf4j
public class PostDataSource implements DataSource<PostVO> {
	
	@Resource
	private PostEsService postEsService;
	
	@Resource
	private PostService postService;
	
	/**
	 * 从ES中搜索帖子
	 *
	 * @param searchRequest 搜索条件
	 * @param request       request
	 * @return {@link Page {@link PostVO }}
	 */
	@Override
	public Page<PostVO> doSearch(SearchRequest searchRequest, HttpServletRequest request) {
		PostQueryRequest postQueryRequest = new PostQueryRequest();
		BeanUtils.copyProperties(searchRequest, postQueryRequest);
		Page<Post> postPage = postEsService.searchPostFromEs(postQueryRequest);
		return postService.getPostVOPage(postPage, request);
	}
}