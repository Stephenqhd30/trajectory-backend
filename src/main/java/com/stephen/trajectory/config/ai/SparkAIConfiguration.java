package com.stephen.trajectory.config.ai;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: stephen qiu
 * @create: 2024-07-01 15:53
 **/
@Configuration
@Data
@ConfigurationProperties(prefix = "xunfei.client")
public class SparkAIConfiguration {
	
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
	
	@Bean
	public SparkClient sparkClient() {
		SparkClient sparkClient = new SparkClient();
		sparkClient.apiKey = apiKey;
		sparkClient.apiSecret = apiSecret;
		sparkClient.appid = appid;
		return sparkClient;
	}
}
