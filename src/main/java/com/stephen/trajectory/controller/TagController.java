package com.stephen.trajectory.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.RedisConstant;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.dto.tag.*;
import com.stephen.trajectory.model.entity.Tag;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.TagVO;
import com.stephen.trajectory.service.TagService;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 标签接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {
	
	@Resource
	private TagService tagService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	
	// region 增删改查
	
	/**
	 * 创建标签
	 *
	 * @param tagAddRequest tagAddRequest
	 * @param request       request
	 * @return BaseResponse<Long>
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(tagAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		Tag tag = new Tag();
		BeanUtils.copyProperties(tagAddRequest, tag);
		// 数据校验
		tagService.validTag(tag, true);
		// todo 填充默认值
		User loginUser = userService.getLoginUser(request);
		tag.setUserId(loginUser.getId());
		// 写入数据库
		boolean result = tagService.save(tag);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newTagId = tag.getId();
		return ResultUtils.success(newTagId);
	}
	
	/**
	 * 删除标签
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Tag oldTag = tagService.getById(id);
		ThrowUtils.throwIf(oldTag == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldTag.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = tagService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新标签（仅管理员可用）
	 *
	 * @param tagUpdateRequest tagUpdateRequest
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/update")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest) {
		if (tagUpdateRequest == null || tagUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Tag tag = new Tag();
		BeanUtils.copyProperties(tagUpdateRequest, tag);
		// 数据校验
		tagService.validTag(tag, false);
		// 判断是否存在
		long id = tagUpdateRequest.getId();
		Tag oldTag = tagService.getById(id);
		ThrowUtils.throwIf(oldTag == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = tagService.updateById(tag);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取标签（封装类）
	 *
	 * @param id id
	 * @return BaseResponse<TagVO>
	 */
	@GetMapping("/get/vo")
	public BaseResponse<TagVO> getTagVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Tag tag = tagService.getById(id);
		ThrowUtils.throwIf(tag == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(tagService.getTagVO(tag, request));
	}
	
	/**
	 * 分页获取标签列表（仅管理员可用）
	 *
	 * @param tagQueryRequest tagQueryRequest
	 * @return BaseResponse<Page < Tag>>
	 */
	@PostMapping("/list/page")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<Tag>> listTagByPage(@RequestBody TagQueryRequest tagQueryRequest) {
		long current = tagQueryRequest.getCurrent();
		long size = tagQueryRequest.getPageSize();
		// 查询数据库
		Page<Tag> tagPage = tagService.page(new Page<>(current, size),
				tagService.getQueryWrapper(tagQueryRequest));
		return ResultUtils.success(tagPage);
	}
	
	/**
	 * 分页获取标签列表（封装类）
	 *
	 * @param tagQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<TagVO>> listTagVOByPage(@RequestBody TagQueryRequest tagQueryRequest,
	                                                 HttpServletRequest request) {
		long current = tagQueryRequest.getCurrent();
		long size = tagQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Tag> tagPage = tagService.page(new Page<>(current, size),
				tagService.getQueryWrapper(tagQueryRequest));
		// 获取封装类
		return ResultUtils.success(tagService.getTagVOPage(tagPage, request));
	}
	
	/**
	 * 分页获取当前登录用户创建的标签列表
	 *
	 * @param tagQueryRequest tagQueryRequest
	 * @param request         request
	 * @return BaseResponse<Page < TagVO>>
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<TagVO>> listMyTagVOByPage(@RequestBody TagQueryRequest tagQueryRequest,
	                                                   HttpServletRequest request) {
		ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
		// 补充查询条件，只查询当前登录用户的数据
		User loginUser = userService.getLoginUser(request);
		tagQueryRequest.setUserId(loginUser.getId());
		long current = tagQueryRequest.getCurrent();
		long size = tagQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Page<Tag> tagPage = tagService.page(new Page<>(current, size),
				tagService.getQueryWrapper(tagQueryRequest));
		// 获取封装类
		return ResultUtils.success(tagService.getTagVOPage(tagPage, request));
	}
	
	/**
	 * 编辑标签（给用户使用）
	 *
	 * @param tagEditRequest tagEditRequest
	 * @param request        request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editTag(@RequestBody TagEditRequest tagEditRequest, HttpServletRequest request) {
		if (tagEditRequest == null || tagEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		Tag tag = new Tag();
		BeanUtils.copyProperties(tagEditRequest, tag);
		// 数据校验
		tagService.validTag(tag, false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = tagEditRequest.getId();
		Tag oldTag = tagService.getById(id);
		ThrowUtils.throwIf(oldTag == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldTag.getUserId().equals(loginUser.getId()) && userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = tagService.updateById(tag);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	// endregion
	
	/**
	 * 获取标签的树组件
	 *
	 * @return {@link BaseResponse}<{@link List}<{@link TagDTO}>>
	 */
	@GetMapping("/list/tree")
	public BaseResponse<List<TagDTO>> listTagByTree() {
		String filepath = RedisConstant.FILE_NAME + RedisConstant.TAG_TREE_KEY;
		// 先从 Redis 中查询是否过期
		if (Boolean.TRUE.equals(redisTemplate.hasKey(filepath))) {
			return ResultUtils.success((List<TagDTO>) redisTemplate.opsForValue().get(filepath));
		}
		// 构建查询条件 查询出父标签
		QueryWrapper<Tag> parentQueryWrapper = new QueryWrapper<>();
		parentQueryWrapper.isNull("parentId");
		// 标签根节点列表
		List<Tag> parentTags = tagService.list(parentQueryWrapper);
		
		// 转换为 TagDTO，并递归填充子节点
		List<TagDTO> tagDTOList = parentTags.stream()
				.map(parentTag -> tagService.getTagDTO(parentTag))
				.collect(Collectors.toList());
		// 返回标签树形结构
		// 将返回的标签属性组件存储进 Redis 中（大部分时间不会背修改）设置过期时间为1天
		redisTemplate.opsForValue().set(filepath, tagDTOList, 1, TimeUnit.DAYS);
		return ResultUtils.success(tagDTOList);
	}
}
