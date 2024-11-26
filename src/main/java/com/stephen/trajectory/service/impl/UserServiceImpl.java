package com.stephen.trajectory.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stephen.trajectory.aop.listener.UserExcelListener;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.secure.utils.DeviceUtils;
import com.stephen.trajectory.constants.CommonConstant;
import com.stephen.trajectory.constants.RedisConstant;
import com.stephen.trajectory.constants.SaltConstant;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.mapper.UserMapper;
import com.stephen.trajectory.model.dto.user.UserMatchRequest;
import com.stephen.trajectory.model.dto.user.UserQueryRequest;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.user.UserGenderEnum;
import com.stephen.trajectory.model.enums.user.UserRoleEnum;
import com.stephen.trajectory.model.vo.LoginUserVO;
import com.stephen.trajectory.model.vo.UserVO;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.regex.RegexUtils;
import com.stephen.trajectory.utils.similarity.CosineSimilarityUtil;
import com.stephen.trajectory.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户服务实现
 *
 * @author stephen qiu
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
	
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	
	/**
	 * 校验数据
	 *
	 * @param user user
	 * @param add  对创建的数据进行校验
	 */
	@Override
	public void validUser(User user, boolean add) {
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
		// todo 从对象中取值
		String userAccount = user.getUserAccount();
		String userName = user.getUserName();
		Integer userGender = user.getUserGender();
		String userProfile = user.getUserProfile();
		String userEmail = user.getUserEmail();
		String userPhone = user.getUserPhone();
		String tags = user.getTags();
		
		// 创建数据时，参数不能为空
		if (add) {
			// todo 补充校验规则
			ThrowUtils.throwIf(StringUtils.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "账号不能为空");
			ThrowUtils.throwIf(StringUtils.isBlank(userName), ErrorCode.PARAMS_ERROR, "用户名不能为空");
		}
		// 修改数据时，有参数则校验
		// todo 补充校验规则
		if (StringUtils.isNotBlank(userAccount)) {
			ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 20, ErrorCode.PARAMS_ERROR, "账号不能小于4位，不能多于20位");
		}
		if (StringUtils.isNotBlank(userName)) {
			ThrowUtils.throwIf(userName.length() < 4 || userName.length() > 20, ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (StringUtils.isNotBlank(userProfile)) {
			ThrowUtils.throwIf(userProfile.length() > 50, ErrorCode.PARAMS_ERROR, "用户简介不能多余50字");
		}
		if (StringUtils.isNotBlank(userEmail)) {
			ThrowUtils.throwIf(!RegexUtils.checkEmail(userEmail), ErrorCode.PARAMS_ERROR, "用户邮箱有误");
		}
		if (StringUtils.isNotBlank(userPhone)) {
			ThrowUtils.throwIf(!RegexUtils.checkMobile(userPhone), ErrorCode.PARAMS_ERROR, "用户手机号码有误");
		}
		if (ObjectUtils.isNotEmpty(userGender)) {
			ThrowUtils.throwIf(UserGenderEnum.getEnumByValue(userGender) == null, ErrorCode.PARAMS_ERROR, "性别填写有误");
		}
		if (StringUtils.isNotBlank(tags)) {
			ThrowUtils.throwIf(tags.length() > 1024, ErrorCode.PARAMS_ERROR, "标签过长,请重新选择适合您的标签");
		}
	}
	
	/**
	 * @param userAccount   用户账户
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return long 注册成功之后的id
	 */
	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		synchronized (userAccount.intern()) {
			// 账户不能重复
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("userAccount", userAccount);
			long count = this.baseMapper.selectCount(queryWrapper);
			if (count > 0) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
			}
			// 2. 加密
			String encryptPassword = DigestUtils.md5DigestAsHex((SaltConstant.SALT + userPassword).getBytes());
			// 3. 插入数据
			User user = new User();
			user.setUserAccount(userAccount);
			user.setUserPassword(encryptPassword);
			// 3. 给用户分配一个默认的头像
			user.setUserAvatar(Optional.ofNullable(user.getUserAvatar()).orElse(UserConstant.USER_AVATAR));
			boolean saveResult = this.save(user);
			if (!saveResult) {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
			}
			return user.getId();
		}
	}
	
	/**
	 * @param userAccount  用户账户
	 * @param userPassword 用户密码
	 * @param request      request
	 * @return {@link LoginUserVO}
	 */
	@Override
	public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 2. 加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SaltConstant.SALT + userPassword).getBytes());
		// 查询用户是否存在
		LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class)
				.eq(User::getUserAccount, userAccount)
				.eq(User::getUserPassword, encryptPassword);
		User user = this.getOne(eq);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 3. 记录用户的登录态
		// 使用Sa-Token登录，并指定设备同端登录互斥
		StpUtil.login(user.getId(), DeviceUtils.getRequestDevice(request));
		StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE, user);
		return this.getLoginUserVO(user);
	}
	
	/**
	 * 获取当前登录用户
	 *
	 * @param request request
	 * @return {@link User}
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 先判断是否已经登录
		Object loginUserId = StpUtil.getLoginIdDefaultNull();
		if (loginUserId == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		}
		// 从数据库查询（追求性能的话可以注释，直接走缓存）
		// StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		User currentUser = this.getById((String) loginUserId);
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
		return currentUser;
	}
	
	/**
	 * 获取当前登录用户（允许未登录）
	 *
	 * @param request request
	 * @return {@link User}
	 */
	@Override
	public User getLoginUserPermitNull(HttpServletRequest request) {
		// 先判断是否已登录
		User currentUser = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		if (currentUser == null || currentUser.getId() == null) {
			return null;
		}
		// 从数据库查询（追求性能的话可以注释，直接走缓存）
		long userId = currentUser.getId();
		return this.getById(userId);
	}
	
	/**
	 * 是否为管理员
	 *
	 * @param request request
	 * @return boolean 是否为管理员
	 */
	@Override
	public boolean isAdmin(HttpServletRequest request) {
		// 仅管理员可查询
		User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		return isAdmin(user);
	}
	
	@Override
	public boolean isAdmin(User user) {
		return user == null || !UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
	}
	
	/**
	 * 用户注销
	 *
	 * @param request request
	 * @return boolean 是否退出成功
	 */
	@Override
	public boolean userLogout(HttpServletRequest request) {
		// 判断是否登录
		StpUtil.checkLogin();
		// 移除登录态
		StpUtil.logout();
		return true;
	}
	
	/**
	 * 获取登录用户视图类
	 *
	 * @param user user
	 * @return {@link LoginUserVO
	 * }
	 */
	@Override
	public LoginUserVO getLoginUserVO(User user) {
		if (user == null) {
			return null;
		}
		// todo 在此处将实体类和 DTO 进行转换
		LoginUserVO loginUserVO = new LoginUserVO();
		BeanUtils.copyProperties(user, loginUserVO);
		String tags = user.getTags();
		loginUserVO.setTags(JSONUtil.toList(tags, String.class));
		// 设置将token保存到登录用户信息中
		loginUserVO.setToken(StpUtil.getTokenInfo().getTokenValue());
		return loginUserVO;
	}
	
	/**
	 * 获取用户VO封装类
	 *
	 * @param user    user
	 * @param request request
	 * @return {@link UserVO}
	 */
	@Override
	public UserVO getUserVO(User user, HttpServletRequest request) {
		// 对象转封装类
		return UserVO.objToVo(user);
	}
	
	
	/**
	 * 获得用户视图类列表
	 *
	 * @param userList userList
	 * @param request  request
	 * @return {@link List<UserVO>}
	 */
	@Override
	public List<UserVO> getUserVO(List<User> userList, HttpServletRequest request) {
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userList.stream().map(user -> getUserVO(user, request)).collect(Collectors.toList());
	}
	
	/**
	 * 获取查询封装类
	 *
	 * @param userQueryRequest userQueryRequest
	 * @return {@link QueryWrapper<User>}
	 */
	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		Long id = userQueryRequest.getId();
		Long notId = userQueryRequest.getNotId();
		String unionId = userQueryRequest.getUnionId();
		String mpOpenId = userQueryRequest.getMpOpenId();
		String userName = userQueryRequest.getUserName();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		Integer userGender = userQueryRequest.getUserGender();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();
		String userEmail = userQueryRequest.getUserEmail();
		String userPhone = userQueryRequest.getUserPhone();
		List<String> tagList = userQueryRequest.getTags();
		String searchText = userQueryRequest.getSearchText();
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		
		// 遍历查询
		if (CollUtil.isNotEmpty(tagList)) {
			for (String tag : tagList) {
				queryWrapper.like("tags", "\"" + tag + "\"");
			}
		}
		// 精准查询
		queryWrapper.eq(id != null, "id", id);
		queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
		queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
		queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
		queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
		queryWrapper.eq(ObjectUtils.isNotEmpty(userGender), "userGender", userGender);
		// 拼接查询条件
		if (StringUtils.isNotBlank(searchText)) {
			queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
		}
		// 模糊查询
		queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
		queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
		queryWrapper.like(StringUtils.isNotBlank(userEmail), "userEmail", userEmail);
		queryWrapper.like(StringUtils.isNotBlank(userPhone), "userPhone", userPhone);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}
	
	/**
	 * 导入用户数据
	 *
	 * @param file 上传的 Excel 文件
	 * @return 返回成功和错误信息
	 */
	@Override
	public Map<String, Object> importUsers(MultipartFile file) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.OPERATION_ERROR, "上传的文件为空");
		
		// 传递 userService 实例给 UserExcelListener
		UserExcelListener listener = new UserExcelListener(this);
		
		try {
			EasyExcel.read(file.getInputStream(), User.class, listener).sheet().doRead();
		} catch (IOException e) {
			log.error("文件读取失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件读取失败");
		} catch (ExcelAnalysisException e) {
			log.error("Excel解析失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "Excel解析失败");
		}
		
		// 返回处理结果，包括成功和异常的数据
		Map<String, Object> result = new HashMap<>();
		// 获取异常记录
		result.put("errorRecords", listener.getErrorRecords());
		log.info("成功导入 {} 条用户数据，{} 条错误数据", listener.getSuccessRecords().size(), listener.getErrorRecords().size());
		
		return result;
	}
	
	/**
	 * 基于余弦相似度匹配用户
	 *
	 * @param userMatchRequest userMatchRequest
	 * @param request          request
	 * @return {@link List<UserVO>}
	 */
	@Override
	public List<UserVO> cosMatchUsers(UserMatchRequest userMatchRequest, HttpServletRequest request) {
		User loginUser = this.getLoginUser(request);
		// 检查 Redis 中是否有缓存的匹配用户数据
		String cacheKey = RedisConstant.FILE_NAME + RedisConstant.MATCH_USER + ":" + loginUser.getId();
		if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
			// 如果缓存存在，直接获取并返回
			String cachedResult = (String) redisTemplate.opsForValue().get(cacheKey);
			return JSONUtil.toList(cachedResult, UserVO.class);
		}
		// 查询所有有标签的用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "tags");
		queryWrapper.isNotNull("tags");
		List<User> userList = this.list(queryWrapper);
		
		// 获取当前登录用户的标签并解析为列表
		String tags = loginUser.getTags();
		List<String> tagList = JSONUtil.toList(tags, String.class);
		
		// 用户与相似度的列表
		List<Pair<User, Double>> similarityList = new ArrayList<>();
		
		// 计算所有用户与当前用户的相似度
		for (User user : userList) {
			// 跳过无标签或当前用户自己
			if (StringUtils.isBlank(user.getTags()) || user.getId().equals(loginUser.getId())) {
				continue;
			}
			
			// 解析用户标签
			List<String> userTagList = JSONUtil.toList(user.getTags(), String.class);
			// 计算余弦相似度
			double similarity = CosineSimilarityUtil.cosineSimilarity(tagList, userTagList);
			if (Double.isNaN(similarity)) {
				similarity = 0;
			}
			// 将用户和相似度存入列表
			similarityList.add(new Pair<>(user, similarity));
		}
		
		// 按照相似度降序排序，并取前 num 个用户
		List<Pair<User, Double>> topUserPairList = similarityList.stream()
				.sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
				.limit(userMatchRequest.getNumber())
				.collect(Collectors.toList());
		
		// 获取匹配用户的ID列表
		List<Long> userIdList = topUserPairList.stream()
				.map(pair -> pair.getKey().getId())
				.collect(Collectors.toList());
		
		// 根据 ID 查询用户详情
		QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
		userQueryWrapper.in("id", userIdList);
		List<User> matchedUsers = this.list(userQueryWrapper);
		
		// 将用户按照最初排序的顺序返回，并设置相似度
		Map<Long, Double> userSimilarityMap = topUserPairList.stream()
				.collect(Collectors.toMap(pair -> pair.getKey().getId(), Pair::getValue));
		
		List<UserVO> userVOList = matchedUsers.stream()
				.map(user -> {
					UserVO userVO = this.getUserVO(user, request);
					// 设置相似度
					userVO.setSimilarity(userSimilarityMap.get(user.getId()));
					return userVO;
				})
				.collect(Collectors.toList());
		// 保存至Redis中 设置过期时间为一小时
		redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(userVOList), 1, TimeUnit.HOURS);
		return userVOList;
	}
	
	/**
	 * 分页获取余弦相似度匹配用户
	 *
	 * @param userVOPage userVOPage
	 * @param loginUser  loginUser
	 * @return {@link Page<UserVO>}
	 */
	@Override
	public Page<UserVO> cosMatchUserByPage(Page<UserVO> userVOPage, User loginUser) {
		// 获取当前登录用户的标签并解析为列表
		String tags = loginUser.getTags();
		List<String> tagList = JSONUtil.toList(tags, String.class);
		// 计算所有用户与当前用户的相似度
		List<UserVO> userVOList = userVOPage.getRecords().stream()
				// 过滤掉当前用户和没有标签的用户
				.filter(user -> !ObjectUtils.isEmpty(user.getTags()) && !user.getId().equals(loginUser.getId()))
				.peek(user -> {
					// 解析用户标签
					List<String> userTagList = user.getTags();
					// 计算余弦相似度
					double similarity = CosineSimilarityUtil.cosineSimilarity(tagList, userTagList);
					if (Double.isNaN(similarity)) {
						similarity = 0;
					}
					// 设置相似度
					user.setSimilarity(similarity);
				})
				// 按照相似度降序排序
				.sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
				.collect(Collectors.toList());
		// 更新分页数据
		userVOPage.setRecords(userVOList);
		return userVOPage;
	}
	
}
