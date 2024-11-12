package com.stephen.trajectory.aop.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.stephen.trajectory.constants.ExcelConstant;
import com.stephen.trajectory.constants.SaltConstant;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.user.UserGenderEnum;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.document.excel.model.ErrorRecord;
import com.stephen.trajectory.utils.document.excel.model.SuccessRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 导入用户 excel文件监听器
 *
 * @author: stephen qiu
 * @create: 2024-09-26 10:36
 **/
@Slf4j
public class UserExcelListener extends AnalysisEventListener<User> {
	
	private final UserService userService;
	
	/**
	 * 有个很重要的点 UserInfoListener 不能被spring管理，
	 * 要每次读取excel都要new,然后里面用到spring可以构造方法传进去
	 *
	 * @param userService userService
	 */
	public UserExcelListener(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * 缓存的证书数据列表，每批次达到BATCH_COUNT后批量插入数据库
	 */
	private final List<User> cachedDataList = ListUtils.newArrayListWithExpectedSize(ExcelConstant.BATCH_COUNT);
	
	/**
	 * 记录异常信息的列表，用于收集处理错误的数据
	 */
	@Getter
	private final List<ErrorRecord<User>> errorRecords = ListUtils.newArrayList();
	
	/**
	 * 记录成功导入的信息，用于收集处理成功的数据
	 */
	@Getter
	private final List<SuccessRecord<User>> successRecords = ListUtils.newArrayList();
	
	/**
	 * 当解析出现异常时调用
	 *
	 * @param exception 异常对象
	 * @param context   上下文信息
	 */
	@Override
	public void onException(Exception exception, AnalysisContext context) throws Exception {
		log.error("解析过程中出现异常：行号={}, 异常信息={}", context.readRowHolder().getRowIndex(), exception.getMessage());
		throw exception;
	}
	
	/**
	 * 当读取到一行数据时，会调用这个方法，并将读取到的数据以及上下文信息作为参数传入
	 * 可以在这个方法中对读取到的数据进行处理和操作，处理数据时要注意异常错误，保证读取数据的稳定性
	 *
	 * @param user    user
	 * @param context context
	 */
	@Override
	public void invoke(User user, AnalysisContext context) {
		User newUser = new User();
		BeanUtils.copyProperties(user, newUser);
		try {
			// 先检查用户传入参数是否合法
			userService.validUser(user, true);
			newUser.setUserGender(Optional.ofNullable(newUser.getUserGender())
					.orElse(UserGenderEnum.SECURITY.getValue()));
			newUser.setUserEmail(Optional.ofNullable(newUser.getUserEmail())
					.orElse("该用户很懒没有设置邮箱"));
			newUser.setUserPhone(Optional.ofNullable(newUser.getUserPhone())
					.orElse("该用户很懒没有设置电话"));
			newUser.setUserAvatar(UserConstant.USER_AVATAR);
			newUser.setUserPassword(DigestUtils.md5DigestAsHex((SaltConstant.SALT + user.getUserPassword()).getBytes()));
			newUser.setUserRole(UserConstant.DEFAULT_ROLE);
			cachedDataList.add(newUser);
			successRecords.add(new SuccessRecord<>(newUser, "成功导入"));
		} catch (Exception e) {
			// 捕获异常并记录
			log.error("处理数据时出现异常: {}", e.getMessage());
			// 将错误的记录信息存储到列表中
			errorRecords.add(new ErrorRecord<>(newUser, e.getMessage()));
		}
		if (cachedDataList.size() >= ExcelConstant.BATCH_COUNT) {
			saveDataAsync();
			cachedDataList.clear();
		}
	}
	
	/**
	 * 数据解析完成后执行的收尾操作
	 *
	 * @param context 上下文信息
	 */
	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		// 处理剩余未保存的数据
		if (!cachedDataList.isEmpty()) {
			saveDataAsync();
			cachedDataList.clear();
		}
		log.info("所有数据解析完成，sheet名称={}！", context.readSheetHolder().getSheetName());
	}
	
	/**
	 * 执行批量保存数据操作
	 */
	private void saveDataAsync() {
		List<User> dataToSave = List.copyOf(cachedDataList);
		CompletableFuture.runAsync(() -> {
			log.info("开始批量保存{}条证书数据到数据库...", dataToSave.size());
			try {
				userService.saveBatch(dataToSave);
				log.info("批量保存数据库成功！");
			} catch (Exception e) {
				log.error("批量保存数据库失败：{}", e.getMessage());
				errorRecords.add(new ErrorRecord<>(null, "批量保存失败：" + e.getMessage()));
			}
		});
	}
}
