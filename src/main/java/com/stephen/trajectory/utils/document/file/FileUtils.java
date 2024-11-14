package com.stephen.trajectory.utils.document.file;

import cn.hutool.core.io.FileUtil;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.model.enums.file.FileUploadBizEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 文件校验工具类
 *
 * @author: stephen qiu
 **/
public class FileUtils {
	
	/**
	 * 校验文件
	 *
	 * @param multipartFile     multipartFile
	 * @param fileUploadBizEnum 业务类型
	 */
	public static void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
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
		if (FileUploadBizEnum.GENERATE_EXCEL.equals(fileUploadBizEnum)) {
			long ONE_M = 10 * 1024 * 1024L;
			if (fileSize > ONE_M) {
				throw new BusinessException(ErrorCode.PARAMS_SIZE_ERROR, "文件大小不能超过 10M");
			}
			if (!Arrays.asList("xlsx", "xls", "csv").contains(fileSuffix)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
			}
		}
	}
}
