package com.stephen.trajectory.config.ai.spark.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SparkAI配置属性
 *
 * @author stephen qiu
 */
@Data
@ConfigurationProperties(prefix = "xunfei.client")
@Configuration
public class SparkAIProperties {
	
	/**
	 * appid
	 */
	private String appid;
	
	/**
	 * apiKey
	 */
	private String apiKey;
	
	
	/**
	 * apiSecret
	 */
	private String apiSecret;
	
}