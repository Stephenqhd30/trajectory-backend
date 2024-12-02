package com.stephen.trajectory.controller;

import com.stephen.trajectory.common.BaseResponse;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ResultUtils;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.manager.oss.MinioManager;
import com.stephen.trajectory.model.dto.file.UploadFileRequest;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.enums.file.FileUploadBizEnum;
import com.stephen.trajectory.service.UserService;
import com.stephen.trajectory.utils.document.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
	private MinioManager minioManager;
	
	/**
	 * 文件上传(使用Minio对象存储)
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
		FileUtils.validFile(multipartFile, fileUploadBizEnum);
		User loginUser = userService.getLoginUser(request);
		
		// 文件目录：根据业务、用户来划分
		String path = String.format("/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId());
		
		try {
			// 直接上传文件
			String s = minioManager.uploadToMinio(multipartFile, path);
			// 返回可访问地址
			return ResultUtils.success(s);
		} catch (IOException e) {
			log.error("文件上传失败, 文件路径为: {}", path, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		}
	}
}
