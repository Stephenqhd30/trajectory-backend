package com.stephen.trajectory.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.trajectory.common.*;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.constants.SaltConstant;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.dto.user.*;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.LoginUserVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * 用户接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
	
	@Resource
	private UserService userService;
	
	// region 登录相关
	
	/**
	 * 用户注册
	 *
	 * @param userRegisterRequest 用户注册请求
	 * @return BaseResponse<Long> 注册是否成功
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
		// 获取请求参数
		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			return null;
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword);
		return ResultUtils.success(result);
	}
	
	/**
	 * 用户登录
	 *
	 * @param userLoginRequest userLoginRequest
	 * @param request          request
	 * @return BaseResponse<LoginUserVO>
	 */
	@PostMapping("/login")
	public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		if (userLoginRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR);
		LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
		return ResultUtils.success(loginUserVO);
	}
	
	/**
	 * 用户注销
	 *
	 * @param request request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/logout")
	public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
		if (request == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = userService.userLogout(request);
		return ResultUtils.success(result);
	}
	
	/**
	 * 获取当前登录用户
	 *
	 * @param request request
	 * @return BaseResponse<LoginUserVO>
	 */
	@GetMapping("/get/login")
	public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
		User user = userService.getLoginUser(request);
		return ResultUtils.success(userService.getLoginUserVO(user));
	}
	
	// endregion
	
	// region 增删改查
	
	/**
	 * 创建用户
	 *
	 * @param userAddRequest userAddRequest
	 * @param request        request
	 * @return BaseResponse<Long>
	 */
	@PostMapping("/add")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
		// todo 在此处将实体类和 DTO 进行转换
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);
		List<String> tagList = userAddRequest.getTags();
		user.setTags(JSONUtil.toJsonStr(tagList));
		// 数据校验
		try {
			userService.validUser(user, true);
		} catch (Exception e) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// todo 填充默认值
		// 默认密码 12345678
		String encryptPassword = DigestUtils.md5DigestAsHex((SaltConstant.SALT + UserConstant.DEFAULT_PASSWORD).getBytes());
		user.setUserPassword(encryptPassword);
		// 设置一个默认的头像
		user.setUserAvatar(Optional.ofNullable(user.getUserAvatar()).orElse(UserConstant.USER_AVATAR));
		// 写入数据库
		boolean result = userService.save(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		// 返回新写入的数据 id
		long newTagId = user.getId();
		return ResultUtils.success(newTagId);
	}
	
	/**
	 * 删除用户
	 *
	 * @param deleteRequest deleteRequest
	 * @param request       request
	 * @return /ioBaseResponse<Boolean>
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
		User user = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		User oldUser = userService.getById(id);
		ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldUser.getId().equals(user.getId()) && userService.isAdmin(request)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = userService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 更新用户
	 *
	 * @param userUpdateRequest userUpdateRequest
	 * @param request           request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/update")
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
	                                        HttpServletRequest request) {
		if (userUpdateRequest == null || userUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// todo 在此处将实体类和 DTO 进行转换
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		List<String> tagList = userUpdateRequest.getTags();
		user.setTags(JSONUtil.toJsonStr(tagList));
		// 数据校验
		try {
			userService.validUser(user, false);
		} catch (Exception e) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// 判断是否存在
		long id = userUpdateRequest.getId();
		User oldUser = userService.getById(id);
		ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
		// 如果用户需要修改密码
		if (StringUtils.isNotBlank(userUpdateRequest.getUserPassword())) {
			// todo 密码加密
			String encryptPassword = DigestUtils.md5DigestAsHex((SaltConstant.SALT + userUpdateRequest.getUserPassword()).getBytes());
			user.setUserPassword(encryptPassword);
		}
		// 操作数据库
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
	/**
	 * 根据 id 获取用户（仅管理员）
	 *
	 * @param id      用户id
	 * @param request request
	 * @return BaseResponse<User>
	 */
	@GetMapping("/get")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		return ResultUtils.success(user);
	}
	
	/**
	 * 根据 id 获取包装类
	 *
	 * @param id      用户id
	 * @param request request
	 * @return 查询得到的用户包装类
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(userService.getUserVO(user, request));
	}
	
	
	/**
	 * 分页获取用户列表（仅管理员）
	 *
	 * @param userQueryRequest userQueryRequest
	 * @param request          request
	 * @return BaseResponse<Page < User>>
	 */
	@PostMapping("/list/page")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                               HttpServletRequest request) {
		long current = userQueryRequest.getCurrent();
		long size = userQueryRequest.getPageSize();
		// todo 在此处将实体类和 DTO 进行转换
		Page<User> userPage = userService.page(new Page<>(current, size),
				userService.getQueryWrapper(userQueryRequest));
		return ResultUtils.success(userPage);
	}
	
	/**
	 * 分页获取用户封装列表
	 *
	 * @param userQueryRequest 用户查询请求
	 * @param request          request
	 * @return BaseResponse<Page < UserVO>>
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
	                                                   HttpServletRequest request) {
		ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long current = userQueryRequest.getCurrent();
		long size = userQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		Page<User> userPage = userService.page(new Page<>(current, size),
				userService.getQueryWrapper(userQueryRequest));
		return ResultUtils.success(userService.getUserVOPage(userPage, request));
	}
	
	// endregion
	
	/**
	 * 更新个人信息
	 *
	 * @param userEditRequest userEditRequest
	 * @param request         request
	 * @return BaseResponse<Boolean>
	 */
	@PostMapping("/update/my")
	public BaseResponse<Boolean> updateMyUser(@RequestBody UserEditRequest userEditRequest,
	                                          HttpServletRequest request) {
		ThrowUtils.throwIf(userEditRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		// todo 在此处将实体类和 DTO 进行转换
		User user = new User();
		BeanUtils.copyProperties(userEditRequest, user);
		List<String> tagList = userEditRequest.getTags();
		user.setTags(JSONUtil.toJsonStr(tagList));
		// 对用户数据进行校验
		try {
			userService.validUser(user, false);
		} catch (Exception e) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
		}
		// 如果用户需要修改密码
		if (StringUtils.isNotBlank(userEditRequest.getUserPassword())) {
			// todo 密码加密
			String encryptPassword = DigestUtils.md5DigestAsHex((SaltConstant.SALT + userEditRequest.getUserPassword()).getBytes());
			user.setUserPassword(encryptPassword);
		}
		user.setId(loginUser.getId());
		user.setEditTime(new Date());
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}
	
}
