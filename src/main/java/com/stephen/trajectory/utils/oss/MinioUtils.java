package com.stephen.trajectory.utils.oss;

import com.stephen.trajectory.config.bean.SpringContextHolder;
import com.stephen.trajectory.manager.oss.MinioManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * MinIO工具类
 *
 * @author stephen qiu
 */
@Slf4j
public class MinioUtils {
	
	/**
	 * 被封装的MinIO对象
	 */
	private static final MinioManager MINIO_MANAGER = SpringContextHolder.getBean(MinioManager.class);
	
	/**
	 * 上传文件
	 *
	 * @param file     上传的文件数据
	 * @param rootPath 文件根目录（注意不需要首尾斜杠，即如果保存文件到"/root/a/"文件夹中，只需要传入"root/a"字符串即可）
	 * @return {@link String}
	 */
	public static String uploadFile(MultipartFile file, String rootPath) throws IOException {
		return MINIO_MANAGER.uploadToMinio(file, rootPath);
	}
	
	
}