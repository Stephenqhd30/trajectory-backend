package com.stephen.trajectory.config.rabbitmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置属性
 *
 * @author stephen qiu
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMqProperties {
	
	/**
	 * 是否开启RabbitMQ
	 */
	private Boolean enable = false;
	
	/**
	 * 获取消息最大等待时间（单位：毫秒）
	 */
	private Long maxAwaitTimeout = 3000L;
	
}