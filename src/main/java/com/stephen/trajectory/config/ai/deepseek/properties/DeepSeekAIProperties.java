package com.stephen.trajectory.config.ai.deepseek.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeekAI配置属性
 *
 * @author stephen qiu
 */
@Data
@ConfigurationProperties(prefix = "deepseek")
@Configuration
public class DeepSeekAIProperties {
	
	
	/**
	 * enabled
	 */
	private Boolean enabled = false;
	
	/**
	 * baseUrl
	 */
	private String baseUrl;
	
	/**
	 * apiKey
	 */
	private String apiKey;
	
}