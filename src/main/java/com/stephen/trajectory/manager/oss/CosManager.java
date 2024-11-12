package com.stephen.trajectory.manager.oss;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.config.oss.cos.condition.CosCondition;
import com.stephen.trajectory.config.oss.cos.properties.CosProperties;
import com.stephen.trajectory.constants.FileConstant;
import com.stephen.trajectory.exception.BusinessException;
import com.stephen.trajectory.model.entity.LogFiles;
import com.stephen.trajectory.model.enums.oss.OssTypeEnum;
import com.stephen.trajectory.service.LogFilesService;
import com.stephen.trajectory.utils.encrypt.SHA3Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cos 对象存储操作
 *
 * @author stephen qiu
 */
@Component
@Slf4j
@Conditional(CosCondition.class)
public class CosManager {
	
	@Resource
	private CosProperties cosProperties;
	
	@Resource
	private COSClient cosClient;
	
	@Resource
	private LogFilesService logFilesService;
	
	/**
	 * 上传对象
	 *
	 * @param key           唯一键
	 * @param localFilePath 本地文件路径
	 */
	public void putObject(String key, String localFilePath) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				new File(localFilePath));
		cosClient.putObject(putObjectRequest);
	}
	
	/**
	 * 上传对象
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public void putObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				file);
		cosClient.putObject(putObjectRequest);
	}
	
	/**
	 * 上传文件到 COS
	 *
	 * @param file 待上传的文件
	 * @param path 上传的路径
	 * @return 文件在 COS 的 URL
	 */
	@Transactional(rollbackFor = Exception.class)
	public String uploadToCos(MultipartFile file, String path) throws IOException {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
		// 获取文件的原始名称和后缀
		String originalName = file.getOriginalFilename();
		String suffix = FilenameUtils.getExtension(originalName);
		long fileSize = file.getSize();
		
		// 生成唯一键
		String uniqueKey = SHA3Utils.encrypt(Arrays.toString(file.getBytes()) + originalName + suffix);
		
		// 查询数据库，看文件是否已存在
		LogFiles existingFile = logFilesService.getOne(
				new LambdaQueryWrapper<LogFiles>()
						.eq(LogFiles::getFileKey, uniqueKey)
						.eq(LogFiles::getFileOssType, OssTypeEnum.COS.getValue())
		);
		
		if (existingFile != null) {
			// 文件已存在，直接返回 URL
			return existingFile.getFileUrl();
		}
		
		String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
		String filePath = (StringUtils.isBlank(path) ? "" : path + "/") + fileName;
		
		try (InputStream inputStream = file.getInputStream()) {
			// 上传到 COS
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(fileSize);
			PutObjectRequest putRequest = new PutObjectRequest(cosProperties.getBucket(), filePath, inputStream, metadata);
			cosClient.putObject(putRequest);
		} catch (IOException | CosClientException e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败" + e.getMessage());
		}
		
		// 保存文件信息
		LogFiles logFile = new LogFiles();
		logFile.setFileKey(uniqueKey);
		logFile.setFileName(fileName);
		logFile.setFileOriginalName(originalName);
		logFile.setFileSuffix(suffix);
		logFile.setFileSize(fileSize);
		logFile.setFileUrl(FileConstant.COS_HOST + filePath);
		logFile.setFileOssType(OssTypeEnum.COS.getValue());
		logFilesService.save(logFile);
		
		return FileConstant.COS_HOST + filePath;
	}
	
	
	/**
	 * 从COS中删除文件
	 *
	 * @param id 文件ID
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteInCosById(Long id) {
		LogFiles fileInDatabase = logFilesService.getOne(
				new LambdaQueryWrapper<LogFiles>()
						.eq(LogFiles::getId, id)
						.eq(LogFiles::getFileOssType, OssTypeEnum.COS.getValue())
		);
		ThrowUtils.throwIf(ObjectUtils.isEmpty(fileInDatabase), ErrorCode.NOT_FOUND_ERROR, "文件不存在");
		
		LambdaQueryWrapper<LogFiles> fileLambdaQueryWrapper = Wrappers.lambdaQuery(LogFiles.class)
				.eq(LogFiles::getFileKey, fileInDatabase.getFileKey())
				.eq(LogFiles::getFileOssType, OssTypeEnum.COS.getValue())
				// 搭配事务给数据库被读行数据加行锁
				.last("FOR UPDATE");
		List<LogFiles> filesInDatabase = logFilesService.list(fileLambdaQueryWrapper);
		if (!logFilesService.removeById(fileInDatabase.getId())) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据有误");
		}
		if (Objects.equals(filesInDatabase.size(), 1)) {
			// 调用通过问价你的 Url 删除文件
			deleteInCosByUrl(fileInDatabase.getFileUrl());
		}
	}
	
	/**
	 * 通过文件的url从COS中删除文件
	 *
	 * @param url 文件URL
	 */
	private void deleteInCosByUrl(String url) {
		ThrowUtils.throwIf(StringUtils.isEmpty(url), ErrorCode.PARAMS_ERROR, "被删除地址为空");
		String bucket = cosProperties.getBucket();
		String[] split = url.split(bucket);
		if (split.length != 2) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "URL格式错误");
		}
		// 移除前导斜杠
		String key = split[1].startsWith("/") ? split[1].substring(1) : split[1];
		try {
			cosClient.deleteObject(bucket, key);
		} catch (CosClientException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败: " + e.getMessage());
		}
	}
}
