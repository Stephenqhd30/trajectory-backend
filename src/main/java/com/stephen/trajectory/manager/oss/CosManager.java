package com.stephen.trajectory.manager.oss;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.oss.cos.condition.CosCondition;
import com.stephen.trajectory.config.oss.cos.properties.CosProperties;
import com.stephen.trajectory.constants.FileConstant;
import com.stephen.trajectory.utils.encrypt.SHA3Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
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
	 * 上传对象（附带图片信息）
	 *
	 * @param key  唯一键
	 * @param file 文件
	 */
	public PutObjectResult putPictureObject(File file, String key) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosProperties.getBucket(), key,
				file);
		// 对图片进行处理（获取基本信息也被视作为一种处理）
		PicOperations picOperations = new PicOperations();
		// 1 表示返回原图信息
		picOperations.setIsPicInfo(1);
		// 构造处理参数
		putObjectRequest.setPicOperations(picOperations);
		return cosClient.putObject(putObjectRequest);
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
		
		return FileConstant.COS_HOST + filePath;
	}
	
	/**
	 * 上传文件(根据地址)
	 *
	 * @param fileUrl          文件地址
	 * @param uploadPathPrefix 上传路径前缀
	 * @return {@link String}
	 * @throws IOException 文件处理异常
	 */
	public String uploadToCos(String fileUrl, String uploadPathPrefix) throws IOException {
		// 图片上传地址
		String uuid = RandomUtil.randomString(16);
		// 从URL提取文件名
		String originFileName = FileUtil.getName(fileUrl);
		String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
				FileUtil.getSuffix(originFileName));
		String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
		File file = null;
		try {
			// 下载文件并创建临时文件
			file = File.createTempFile("upload_", null);
			downloadFile(fileUrl, file);
			// 上传图片
			this.putPictureObject(file, uploadPath);
			// 返回图片地址
			return FileConstant.COS_HOST + "/" + uploadPath;
		} catch (Exception e) {
			log.error("图片上传到对象存储失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		} finally {
			this.deleteTempFile(file);
		}
	}
	
	/**
	 * 从指定URL下载文件到目标文件
	 *
	 * @param fileUrl  文件地址
	 * @param destFile 目标文件
	 */
	public void downloadFile(String fileUrl, File destFile) {
		try (InputStream inputStream = new URL(fileUrl).openStream();
		     FileOutputStream outputStream = new FileOutputStream(destFile)) {
			// 将输入流数据写入到目标文件
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			log.error("下载文件失败", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
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
	
	/**
	 * 删除临时文件
	 */
	public void deleteTempFile(File file) {
		if (file == null) {
			return;
		}
		// 删除临时文件
		boolean deleteResult = file.delete();
		if (!deleteResult) {
			log.error("file delete error, filepath = {}", file.getAbsolutePath());
		}
	}
	
}
