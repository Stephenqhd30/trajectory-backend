package com.stephen.trajectory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.mapper.PostFavourMapper;
import com.stephen.trajectory.mapper.PostMapper;
import com.stephen.trajectory.mapper.PostThumbMapper;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.PostFavour;
import com.stephen.trajectory.model.entity.PostThumb;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
	
	@Resource
	private UserService userService;
	
	@Resource
	private PostThumbMapper postThumbMapper;
	
	@Resource
	private PostFavourMapper postFavourMapper;
	
	/**
	 * 校验帖子信息
	 *
	 * @param post post
	 * @param add  add 是否是添加
	 */
	@Override
	public void validPost(Post post, boolean add) {
		if (post == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String title = post.getTitle();
		String content = post.getContent();
		// 创建时，参数不能为空
		if (add) {
			ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR, "标题不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR, "内容不能为空");
		}
		// 有参数则校验
		if (StringUtils.isNotBlank(title) && title.length() > 80) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
		}
	}
	
	/**
	 * 获取查询包装类
	 *
	 * @param postQueryRequest postQueryRequest
	 * @return {@link QueryWrapper<Post>}
	 */
	@Override
	public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
		QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
		if (postQueryRequest == null) {
			return queryWrapper;
		}
		String searchText = postQueryRequest.getSearchText();
		String sortField = postQueryRequest.getSortField();
		String sortOrder = postQueryRequest.getSortOrder();
		Long id = postQueryRequest.getId();
		String title = postQueryRequest.getTitle();
		String content = postQueryRequest.getContent();
		List<String> tagList = postQueryRequest.getTags();
		Long userId = postQueryRequest.getUserId();
		Long notId = postQueryRequest.getNotId();
		// 拼接查询条件
		if (StringUtils.isNotBlank(searchText)) {
			queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
		queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		// 遍历查询
		if (CollUtil.isNotEmpty(tagList)) {
			for (String tag : tagList) {
				queryWrapper.like("tags", "\"" + tag + "\"");
			}
		}
		// 精准查询
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取文章视图类
	 *
	 * @param post    post
	 * @param request request
	 * @return {@link PostVO}
	 */
	@Override
	public PostVO getPostVO(Post post, HttpServletRequest request) {
		PostVO postVO = PostVO.objToVo(post);
		long postId = post.getId();
		// 1. 关联查询用户信息
		Long userId = post.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		postVO.setUserVO(userVO);
		// 2. 已登录，获取用户点赞、收藏状态
		User loginUser = userService.getLoginUserPermitNull(request);
		if (loginUser != null) {
			// 获取点赞
			QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
			postThumbQueryWrapper.in("postId", postId);
			postThumbQueryWrapper.eq("userId", loginUser.getId());
			PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
			postVO.setHasThumb(postThumb != null);
			// 获取收藏
			QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
			postFavourQueryWrapper.in("postId", postId);
			postFavourQueryWrapper.eq("userId", loginUser.getId());
			PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
			postVO.setHasFavour(postFavour != null);
		}
		return postVO;
	}
	
	/**
	 * 分页获取文章视图类
	 *
	 * @param postPage postPage
	 * @param request  request
	 * @return {@link Page<PostVO>}
	 */
	@Override
	public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
		List<Post> postList = postPage.getRecords();
		Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
		if (CollUtil.isEmpty(postList)) {
			return postVOPage;
		}
		// 1. 异步获取用户信息
		CompletableFuture<Map<Long, List<User>>> userMapFuture = CompletableFuture.supplyAsync(() -> {
			Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
			return userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId));
		});
		// 2. 异步获取点赞状态
		CompletableFuture<Map<Long, Boolean>> thumbMapFuture = CompletableFuture.supplyAsync(() -> {
			Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
			User loginUser = userService.getLoginUserPermitNull(request);
			if (loginUser != null) {
				Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
				QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
				postThumbQueryWrapper.in("postId", postIdSet);
				postThumbQueryWrapper.eq("userId", loginUser.getId());
				postThumbMapper.selectList(postThumbQueryWrapper)
						.forEach(postThumb -> postIdHasThumbMap.put(postThumb.getPostId(), true));
			}
			return postIdHasThumbMap;
		});
		// 3. 异步获取收藏状态
		CompletableFuture<Map<Long, Boolean>> favourMapFuture = CompletableFuture.supplyAsync(() -> {
			Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
			User loginUser = userService.getLoginUserPermitNull(request);
			if (loginUser != null) {
				Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
				QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
				postFavourQueryWrapper.in("postId", postIdSet);
				postFavourQueryWrapper.eq("userId", loginUser.getId());
				postFavourMapper.selectList(postFavourQueryWrapper)
						.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
			}
			return postIdHasFavourMap;
		});
		
		try {
			// 获取异步执行结果
			Map<Long, List<User>> userIdUserListMap = userMapFuture.get();
			Map<Long, Boolean> postIdHasThumbMap = thumbMapFuture.get();
			Map<Long, Boolean> postIdHasFavourMap = favourMapFuture.get();
			
			// 填充信息
			List<PostVO> postVOList = postList.stream().map(post -> {
				PostVO postVO = PostVO.objToVo(post);
				Long userId = post.getUserId();
				User user = null;
				if (userIdUserListMap.containsKey(userId)) {
					user = userIdUserListMap.get(userId).get(0);
				}
				postVO.setUserVO(userService.getUserVO(user, request));
				postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
				postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
				return postVO;
			}).collect(Collectors.toList());
			postVOPage.setRecords(postVOList);
		} catch (InterruptedException | ExecutionException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取帖子信息失败");
		}
		return postVOPage;
	}
}