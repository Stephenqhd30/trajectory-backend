package com.stephen.trajectory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.mapper.TagMapper;
import com.stephen.trajectory.model.dto.tag.TagDTO;
import com.stephen.trajectory.model.dto.tag.TagQueryRequest;
import com.stephen.trajectory.model.entity.Tag;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.TagVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.TagService;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
	
	@Resource
	private UserService userService;
	
	/**
	 * 校验数据
	 *
	 * @param tag tag
	 * @param add 对创建的数据进行校验
	 */
	@Override
	public void validTag(Tag tag, boolean add) {
		ThrowUtils.throwIf(tag == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String tagName = tag.getTagName();
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(tagName), ErrorCode.PARAMS_ERROR);
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(tagName)) {
			ThrowUtils.throwIf(tagName.length() > 20, ErrorCode.PARAMS_ERROR, "标题过长");
		}
	}
	
	/**
	 * 获取查询条件
	 *
	 * @param tagQueryRequest tagQueryRequest
	 * @return {@link QueryWrapper<Tag>}
	 */
	@Override
	public QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest) {
		QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
		if (tagQueryRequest == null) {
			return queryWrapper;
		}
		// todo 从对象中取值
		Long id = tagQueryRequest.getId();
		String tagName = tagQueryRequest.getTagName();
		Long userId = tagQueryRequest.getUserId();
		Long parentId = tagQueryRequest.getParentId();
		Integer isParent = tagQueryRequest.getIsParent();
		String sortField = tagQueryRequest.getSortField();
		String sortOrder = tagQueryRequest.getSortOrder();
		
		// todo 补充需要的查询条件
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(tagName), "tagName", tagName);
		
		// 精确查询
		queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), "parentId", parentId);
		queryWrapper.eq(ObjectUtils.isNotEmpty(isParent), "isParent", isParent);
		// 排序规则
		queryWrapper.orderBy(SqlUtils.validSortField(sortField),
				sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 获取标签封装
	 *
	 * @param tag     tag
	 * @param request request
	 * @return {@link TagVO}
	 */
	@Override
	public TagVO getTagVO(Tag tag, HttpServletRequest request) {
		// 对象转封装类
		TagVO tagVO = TagVO.objToVo(tag);
		
		// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
		// region 可选
		// 1. 关联查询用户信息
		Long userId = tag.getUserId();
		User user = null;
		if (userId != null && userId > 0) {
			user = userService.getById(userId);
		}
		UserVO userVO = userService.getUserVO(user, request);
		tagVO.setUserVO(userVO);
		// endregion
		
		return tagVO;
	}
	
	/**
	 * 分页获取标签封装
	 *
	 * @param tagPage tagPage
	 * @param request request
	 * @return {@link Page<TagVO>}
	 */
	@Override
	public Page<TagVO> getTagVOPage(Page<Tag> tagPage, HttpServletRequest request) {
		List<Tag> tagList = tagPage.getRecords();
		Page<TagVO> tagVOPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
		if (CollUtil.isEmpty(tagList)) {
			return tagVOPage;
		}
		// 1. 异步获取对象列表 => 封装对象列表
		CompletableFuture<List<TagVO>> tagListFuture = CompletableFuture.supplyAsync(() -> tagList.stream().map(TagVO::objToVo).collect(Collectors.toList()));
		// 2. 异步获取用户信息 => 封装用户信息
		CompletableFuture<Map<Long, List<User>>> userMapFurture = CompletableFuture.supplyAsync(() -> {
			Set<Long> userIdSet = tagList.stream().map(Tag::getUserId).collect(Collectors.toSet());
			return userService.listByIds(userIdSet).stream()
					.collect(Collectors.groupingBy(User::getId));
			
		});
		
		try {
			// todo 可以根据需要为封装对象补充值，不需要的内容可以删除
			List<TagVO> tagVOList = tagListFuture.get();
			// 1. 关联查询用户信息
			Map<Long, List<User>> userIdUserListMap = userMapFurture.get();
			// 填充信息
			tagVOList.forEach(tagVO -> {
				Long userId = tagVO.getUserId();
				User user = null;
				if (userIdUserListMap.containsKey(userId)) {
					user = userIdUserListMap.get(userId).get(0);
				}
				tagVO.setUserVO(userService.getUserVO(user, request));
			});
			tagVOPage.setRecords(tagVOList);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取标签失败");
		}
		return tagVOPage;
	}
	
	/**
	 * 递归将 Tag 实体转换为 TagDTO，并填充子标签
	 *
	 * @param tag 父标签
	 * @return {@link TagDTO}
	 */
	@Override
	public TagDTO getTagDTO(Tag tag) {
		// 创建 TagDTO 并填充基础信息
		TagDTO tagDTO = new TagDTO();
		tagDTO.setId(tag.getId());
		tagDTO.setTagName(tag.getTagName());
		
		// 查询子标签
		QueryWrapper<Tag> childQueryWrapper = new QueryWrapper<>();
		childQueryWrapper.eq("parentId", tag.getId());
		List<Tag> childTags = this.list(childQueryWrapper);
		
		// 如果有子标签，递归处理子标签
		if (!childTags.isEmpty()) {
			List<TagDTO.TagChildren> childDTOList = childTags.stream()
					.map(childTag -> {
						if (childTag.getId() == null) {
							return null;
						}
						TagDTO.TagChildren childDTO = new TagDTO.TagChildren();
						childDTO.setId(childTag.getId());
						childDTO.setTagName(childTag.getTagName());
						// 递归调用获取子标签的 DTO
						childDTO.setChildren(Optional.ofNullable(this.getTagDTO(childTag).getChildren()).orElse(new ArrayList<>()));
						
						return childDTO;
					}).filter(Objects::nonNull).collect(Collectors.toList());
			
			tagDTO.setChildren(childDTOList);
		} else {
			// 确保即使没有子标签也将 children 设置为一个空数组
			tagDTO.setChildren(new ArrayList<>());
		}
		
		return tagDTO;
	}
	
}
