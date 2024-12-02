package com.stephen.trajectory.config.redisson;


import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.config.redisson.properties.RedissonProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Redisson 配置类
 * 用于配置 RedissonClient，以连接 Redis 实例
 *
 * @author: stephen qiu
 **/
@Configuration
@Conditional(RedissonCondition.class)
@Slf4j
public class RedissonConfiguration {
	
	@Resource
	private RedissonProperties redissonProperties;
	
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
				.setDatabase(redissonProperties.getDatabase())
				.setPassword(redissonProperties.getPassword());
		// 2. 创建一个 RedissonClient 实例
		// 同步和异步 API
		return Redisson.create(config);
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
}
