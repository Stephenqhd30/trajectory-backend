package com.stephen.trajectory.config.redisson;

import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.config.redisson.properties.RedissonProperties;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * 用于配置 RedissonClient，以连接 Redis 实例
 *
 * @author: stephen qiu
 **/
@Configuration
@Data
@Conditional(RedissonCondition.class)
public class RedissonConfiguration {
	
	private final RedissonProperties redissonProperties;
	
	/**
	 * 配置 RedissonClient 实例
	 *
	 * @return 配置好的 RedissonClient 实例
	 */
	@Bean
	public RedissonClient redissonClient() {
		// 1. 创建配置
		Config config = new Config();
		// 构建 Redis 地址
		String redisAddress = String.format("redis://%s:%s", redissonProperties.getHost(), redissonProperties.getPort());
		// 使用单节点模式配置
		config.useSingleServer()
				.setAddress(redisAddress)
				.setPassword(redissonProperties.getPassword())
				.setDatabase(redissonProperties.getDatabase());
		
		// 2. 创建一个 RedissonClient 实例
		// 同步和异步 API
		return Redisson.create(config);
	}
}
