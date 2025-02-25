package com.stephen.trajectory.config.ai.spark;

import com.stephen.trajectory.config.ai.spark.condition.SparkAICondition;
import com.stephen.trajectory.config.ai.spark.properties.SparkAIProperties;
import io.github.briqt.spark4j.SparkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 讯飞星火AI调用
 *
 * @author: stephen qiu
 **/
@Configuration
@Slf4j
@Conditional(SparkAICondition.class)
public class SparkAIConfiguration {
	
	@Resource
	private SparkAIProperties properties;
	
	
	@Bean("sparkClient")
	public SparkClient sparkClient() {
		SparkClient sparkClient = new SparkClient();
		sparkClient.apiKey = properties.getApiKey();
		sparkClient.apiSecret = properties.getApiSecret();
		sparkClient.appid = properties.getAppid();
		return sparkClient;
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
}
