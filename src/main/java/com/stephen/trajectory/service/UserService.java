package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.dto.user.UserMatchRequest;
import com.stephen.trajectory.model.dto.user.UserQueryRequest;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.LoginUserVO;
import com.stephen.trajectory.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author stephen qiu
 */
public interface UserService extends IService<User> {
	
	/**
	 * 校验用户参数
	 *
	 * @param user user
	 * @param add  是否是添加
	 */
	void validUser(User user, boolean add);
	
	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return long 新用户 id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);
	
	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request      request
	 * @return {@link LoginUserVO}
	 */
	LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
	
	/**
	 * 获取当前登录用户
	 *
	 * @param request request
	 * @return {@link User}
	 */
	User getLoginUser(HttpServletRequest request);
	
	/**
	 * 获取当前登录用户（允许未登录）
	 *
	 * @param request request
	 * @return {@link User}
	 */
	User getLoginUserPermitNull(HttpServletRequest request);
	
	/**
	 * 是否为管理员
	 *
	 * @param request request
	 * @return boolean 是否为管理员
	 */
	boolean isAdmin(HttpServletRequest request);
	
	/**
	 * 是否为管理员
	 *
	 * @param user user
	 * @return boolean 是否为管理员
	 */
	boolean isAdmin(User user);
	
	/**
	 * 用户注销
	 *
	 * @param request request
	 * @return boolean 用户注销
	 */
	boolean userLogout(HttpServletRequest request);
	
	/**
	 * 获取脱敏的已登录用户信息
	 *
	 * @return {@link LoginUserVO}
	 */
	LoginUserVO getLoginUserVO(User user);
	
	/**
	 * 获取脱敏的用户信息
	 *
	 * @param user    user
	 * @param request request
	 * @return {@link UserVO}
	 */
	UserVO getUserVO(User user, HttpServletRequest request);
	
	
	/**
	 * 获取脱敏的用户信息
	 *
	 * @param userList userList
	 * @return {@link List<UserVO>}
	 */
	List<UserVO> getUserVO(List<User> userList, HttpServletRequest request);
	
	/**
	 * 分页获取用户视图类
	 *
	 * @param userPage userPage
	 * @param request  request
	 * @return {@link Page {@link UserVO} }
	 */
	Page<UserVO> getUserVOPage(Page<User> userPage, HttpServletRequest request);
	
	/**
	 * 获取查询条件
	 *
	 * @param userQueryRequest userQueryRequest
	 * @return {@link QueryWrapper<User>}
	 */
	QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
	
	/**
	 * 导入用户
	 *
	 * @param file file
	 * @return {@link Map}<{@link String}, {@link Object}>
	 */
	Map<String, Object> importUsers(MultipartFile file);
}
