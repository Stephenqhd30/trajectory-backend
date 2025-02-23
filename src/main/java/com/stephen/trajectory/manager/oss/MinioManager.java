package com.stephen.trajectory.manager.oss;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.oss.minio.condition.MinioCondition;
import com.stephen.trajectory.config.oss.minio.properties.MinioProperties;
import com.stephen.trajectory.constants.FileConstant;
import com.stephen.trajectory.model.enums.oss.OssTypeEnum;
import com.stephen.trajectory.utils.encrypt.SHA3Utils;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * MinIO配置
 *
 * @author stephen qiu
 */
@Component
@Slf4j
@Conditional(MinioCondition.class)
public class MinioManager {
	
	@Resource
	private MinioProperties minioProperties;
	
	@Resource
	private MinioClient minioClient;
	
	/**
	 * 上传文件到 MinIO
	 *
	 * @param file 待上传的文件
	 * @param path 上传的路径
	 * @return {@link String}
	 */
	@Transactional(rollbackFor = Exception.class)
	public String uploadToMinio(MultipartFile file, String path) throws IOException {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
		
		// 获取文件的原始名称和后缀
		String originalName = StringUtils.defaultIfBlank(file.getOriginalFilename(), file.getName());
		String suffix = FilenameUtils.getExtension(originalName);
		long fileSize = file.getSize();
		
		// 生成唯一键
		String uniqueKey = SHA3Utils.encrypt(Arrays.toString(file.getBytes()) + originalName + suffix);
		String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
		String filePath = String.format("%s/%s", path, fileName);
		
		try (InputStream inputStream = file.getInputStream()) {
			// 读取文件内容
			byte[] dataBytes = inputStream.readAllBytes();
			String key = StringUtils.isBlank(filePath) ? fileName : filePath;
			
			// 上传文件到 MinIO
			minioClient.putObject(PutObjectArgs.builder()
					.bucket(minioProperties.getBucket())
					.object(key)
					.stream(new ByteArrayInputStream(dataBytes), dataBytes.length, -1)
					.build());
			
			
		} catch (Exception e) {
			log.error("文件上传失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
		}
		
		return FileConstant.MINIO_HOST + filePath;
	}
	
	/**
	 * 从MinIO中彻底删除文件
	 *
	 * @param url 文件URL
	 */
	private void deleteInMinioByUrl(String url) {
		ThrowUtils.throwIf(StringUtils.isEmpty(url), ErrorCode.NOT_FOUND_ERROR, "被删除地址为空");
		String[] split = url.split(minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/");
		ThrowUtils.throwIf(split.length != 2, ErrorCode.NOT_FOUND_ERROR, "文件不存在");
		String key = split[1];
		try {
			minioClient.removeObject(RemoveObjectArgs.builder()
					.bucket(minioProperties.getBucket())
					.object(key).build());
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
	}
}
