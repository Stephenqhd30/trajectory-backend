package com.stephen.trajectory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.mapper.PostCommentMapper;
import com.stephen.trajectory.model.dto.postComment.PostCommentQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.model.entity.PostComment;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.PostCommentVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.PostCommentService;
import com.stephen.trajectory.service.PostService;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 帖子评论服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {
	
	@Resource
	private UserService userService;
	
	@Resource
	private PostService postService;
	
	/**
	 * 校验数据
	 *
	 * @param postComment postComment
	 * @param add         对创建的数据进行校验
	 */
	@Override
	public void validPostComment(PostComment postComment, boolean add) {
		ThrowUtils.throwIf(postComment == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		Long postId = postComment.getPostId();
		Long rootId = postComment.getRootId();
		String content = postComment.getContent();
		Long toUid = postComment.getToUid();
		Long toCommentId = postComment.getToCommentId();
		
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(ObjectUtils.isNotEmpty(postId), ErrorCode.PARAMS_ERROR, "帖子id不能为空");
			ThrowUtils.throwIf(ObjectUtils.isNotEmpty(toUid), ErrorCode.PARAMS_ERROR, "被评论人id不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR, "评论内容不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(content)) {
			ThrowUtils.throwIf(content.length() > 4096, ErrorCode.PARAMS_ERROR, "评论内容过长");
		}
		if (ObjectUtils.isNotEmpty(postId)) {
			Post post = postService.getById(postId);
			ThrowUtils.throwIf(post == null, ErrorCode.PARAMS_ERROR, "帖子内容不存在");
		}
		if (ObjectUtils.isNotEmpty(rootId)) {
			PostComment rootPostComment = this.getById(rootId);
			ThrowUtils.throwIf(rootPostComment == null, ErrorCode.PARAMS_ERROR, "根评论不存在");
		}
		if (ObjectUtils.isNotEmpty(toUid)) {
			User toUser = userService.getById(toUid);
			ThrowUtils.throwIf(toUser == null, ErrorCode.PARAMS_ERROR, "被评论用户不能为空");
		}
		if (ObjectUtils.isNotEmpty(toCommentId) && ObjectUtils.isNotEmpty(rootId)) {
			PostComment toComment = this.getById(toCommentId);
			ThrowUtils.throwIf(toComment == null, ErrorCode.PARAMS_ERROR, "被评论的评论不能为空");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param postCommentQueryRequest postCommentQueryRequest
	 * @return {@link QueryWrapper<PostComment>}
	 */
	@Override
	public QueryWrapper<PostComment> getQueryWrapper(PostCommentQueryRequest postCommentQueryRequest) {
		QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
		if (postCommentQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = postCommentQueryRequest.getId();
		Long notId = postCommentQueryRequest.getNotId();
		Long postId = postCommentQueryRequest.getPostId();
		Long rootId = postCommentQueryRequest.getRootId();
		String content = postCommentQueryRequest.getContent();
		Long userId = postCommentQueryRequest.getUserId();
		Long toUid = postCommentQueryRequest.getToUid();
		Long toCommentId = postCommentQueryRequest.getToCommentId();
		String sortField = postCommentQueryRequest.getSortField();
		String sortOrder = postCommentQueryRequest.getSortOrder();
		// todo 补充需要的查询条件
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
		// 精确查询
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(postId), "postId", postId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(rootId), "rootId", rootId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(toUid), "toUid", toUid);
		queryWrapper.eq(ObjectUtils.isNotEmpty(toCommentId), "toCommentId", toCommentId);
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取帖子评论封装
	 *
	 * @param postComment postComment
	 * @param request     request
	 * @return {@link PostCommentVO}
	 */
	@Override
	public PostCommentVO getPostCommentVO(PostComment postComment, HttpServletRequest request) {
		// 对象转封装类
		PostCommentVO postCommentVO = PostCommentVO.objToVo(postComment);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = postComment.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		postCommentVO.setUserVO(userVO);
		
		// endregion
		return postCommentVO;
	}
	
	/**
	 * 分页获取帖子评论封装
	 *
	 * @param postCommentPage postCommentPage
	 * @param request         request
	 * @return {@link Page<PostCommentVO>}
	 */
	@Override
	public Page<PostCommentVO> getPostCommentVOPage(Page<PostComment> postCommentPage, HttpServletRequest request) {
		List<PostComment> postCommentList = postCommentPage.getRecords();
		Page<PostCommentVO> postCommentVOPage = new Page<>(postCommentPage.getCurrent(), postCommentPage.getSize(), postCommentPage.getTotal());
		if (CollUtil.isEmpty(postCommentList)) {
			return postCommentVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PostCommentVO> postCommentVOList = postCommentList.stream()
				.map(PostCommentVO::objToVo)
				.collect(Collectors.toList());
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Set<Long> userIdSet = postCommentList.stream().map(PostComment::getUserId).collect(Collectors.toSet());
		// 填充信息
		if (CollUtil.isNotEmpty(userIdSet)) {
			CompletableFuture<Map<Long, List<User>>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId)));
			try {
				Map<Long, List<User>> userIdUserListMap = mapCompletableFuture.get();
				// 填充信息
				postCommentVOList.forEach(postCommentVO -> {
					Long userId = postCommentVO.getUserId();
					User user = null;
					if (userIdUserListMap.containsKey(userId)) {
						user = userIdUserListMap.get(userId).get(0);
					}
					postCommentVO.setUserVO(userService.getUserVO(user, request));
				});
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取信息失败" + e.getMessage());
			}
		}
		// endregion
		postCommentVOPage.setRecords(postCommentVOList);
		return postCommentVOPage;
	}
	
}
