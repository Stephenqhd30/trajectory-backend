package com.stephen.trajectory.config.oss.cos.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: stephen qiu
 * @create: 2024-11-07 12:58
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "cos.client")
public class CosProperties {
	
	/**
	 * 是否开启腾讯云对象存储客户端功能
	 */
	private Boolean enable = false;
	
	/**
	 * accessKey
	 */
	private String accessKey;
	
	/**
	 * secretKey
	 */
	private String secretKey;
	
	/**
	 * 区域
	 */
	private String region;
	
	/**
	 * 桶名
	 */
	private String bucket;
}
