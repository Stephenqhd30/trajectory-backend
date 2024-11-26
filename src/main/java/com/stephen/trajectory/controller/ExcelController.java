package com.stephen.trajectory.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.constants.UserConstant;
import com.stephen.trajectory.model.enums.user.UserGenderEnum;
import com.stephen.trajectory.model.enums.user.UserRoleEnum;
import com.stephen.trajectory.model.vo.UserExcelVO;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.document.excel.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 导入导出信息接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/excel")
@Slf4j
public class ExcelController {
	
	@Resource
	private UserService userService;
	
	
	/**
	 * 用户数据批量导入
	 *
	 * @param file 用户 Excel 文件
	 * @return 导入结果
	 */
	@PostMapping("/user/import")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public BaseResponse<Map<String, Object>> importUserDataByExcel(@RequestPart("file") MultipartFile file) {
		// 检查文件是否为空
		ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
		
		// 获取文件名并检查是否为null
		String filename = file.getOriginalFilename();
		ThrowUtils.throwIf(filename == null, ErrorCode.PARAMS_ERROR, "文件名不能为空");
		
		// 检查文件格式是否为Excel格式
		if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
			throw new RuntimeException("上传文件格式不正确");
		}
		Map<String, Object> result;
		
		try {
			// 调用服务层处理用户导入
			result = userService.importUsers(file);
		} catch (Exception e) {
			return ResultUtils.error(ErrorCode.PARAMS_ERROR, "导入信息有误");
		}
		return ResultUtils.success(result);
	}
	
	/**
	 * 用户数据导出
	 * 文件下载（失败了会返回一个有部分数据的Excel）
	 * 1. 创建excel对应的实体对象
	 * 2. 设置返回的 参数
	 * 3. 直接写，这里注意，finish的时候会自动关闭OutputStream,当然你外面再关闭流问题不大
	 *
	 * @param response response
	 */
	@GetMapping("/user/download")
	@SaCheckRole(UserConstant.ADMIN_ROLE)
	public void download(HttpServletResponse response) throws IOException {
		// 获取数据，根据自身业务修改
		List<UserExcelVO> userExcelVOList = userService.list().stream().map(user -> {
					UserExcelVO userExcelVO = new UserExcelVO();
					BeanUtils.copyProperties(user, userExcelVO);
					userExcelVO.setId(String.valueOf(user.getId()));
					userExcelVO.setUserGender(Objects.requireNonNull(UserGenderEnum.getEnumByValue(user.getUserGender())).getText());
					userExcelVO.setUserRole(Objects.requireNonNull(UserRoleEnum.getEnumByValue(user.getUserRole())).getText());
					return userExcelVO;
				})
				.collect(Collectors.toList());
		// 设置导出名称
		ExcelUtils.setExcelResponseProp(response, "用户信息");
		// 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
		// 写入 Excel 文件
		try {
			EasyExcel.write(response.getOutputStream(), UserExcelVO.class)
					.sheet("用户信息")
					.doWrite(userExcelVOList);
		} catch (Exception e) {
			log.error("导出失败:{}", e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败");
		}
	}
}
