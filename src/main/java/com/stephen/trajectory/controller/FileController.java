package com.stephen.trajectory.controller;

import cn.hutool.core.io.FileUtil;
import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.manager.oss.CosManager;
import com.stephen.trajectory.model.dto.file.UploadFileRequest;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.file.FileUploadBizEnum;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

/**
 * 文件接口
 *
 * @author stephen qiu
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
	
	@Resource
	private UserService userService;
	
	@Resource
	private CosManager cosManager;
	
	/**
	 * 文件上传(使用COS对象存储)
	 *
	 * @param multipartFile     multipartFile
	 * @param uploadFileRequest uploadFileRequest
	 * @param request           request
	 * @return BaseResponse<String>
	 */
	@PostMapping("/upload")
	public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
	                                       UploadFileRequest uploadFileRequest, HttpServletRequest request) {
		String biz = uploadFileRequest.getBiz();
		FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
		ThrowUtils.throwIf(fileUploadBizEnum == null, ErrorCode.PARAMS_ERROR, "文件上传有误");
		
		// 校验文件类型
		validFile(multipartFile, fileUploadBizEnum);
		User loginUser = userService.getLoginUser(request);
		
		// 文件目录：根据业务、用户来划分
		String path = String.format("/%s/%s/%s", "trajectory", fileUploadBizEnum.getValue(), loginUser.getId());
		
		try {
			// 直接上传文件
			String s = cosManager.uploadToCos(multipartFile, path);
			// 返回可访问地址
			return ResultUtils.success(s);
		} catch (IOException e) {
			log.error("文件上床失败, 文件路径为: {}", path, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		}
	}
	
	/**
	 * 校验文件
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	public void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
		// 文件大小
		long fileSize = multipartFile.getSize();
		// 文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
			long ONE_M = 5 * 1024 * 1024L;
			if (fileSize > ONE_M) {
				throw new BusinessException(ErrorCode.PARAMS_SIZE_ERROR, "文件大小不能超过 5M");
			}
			if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
			}
		}
	}
}
