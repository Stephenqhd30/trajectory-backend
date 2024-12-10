package com.stephen.trajectory.manager.redis;

import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.utils.redisson.rateLimit.RateLimitUtils;
import com.stephen.trajectory.utils.redisson.rateLimit.model.TimeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson 实现的 Redis 限流管理器
 *
 * @author: stephen qiu
 **/
@Component
@Slf4j
@Conditional(RedissonCondition.class)
public class RedisLimiterManager {
	
	/**
	 * 限流操作
	 *
	 * @param key 用于区分不同的限流器
	 */
	public void doRateLimit(String key) {
		// 调用 RedissonClient 的限流方法
		RateLimitUtils.doRateLimit(key, new TimeModel(1L, TimeUnit.SECONDS), 2L, 1L);
	}
}
