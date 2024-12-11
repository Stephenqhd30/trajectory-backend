package com.stephen.trajectory.manager.redis;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.utils.redisson.rateLimit.RateLimitUtils;
import com.stephen.trajectory.utils.redisson.rateLimit.model.TimeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * 基于 Redisson 实现的 Redis 限流管理器
 *
 * @author: stephen qiu
 */
@Component
@Slf4j
@Conditional(RedissonCondition.class)
public class RedisLimiterManager {
	
	/**
	 * 执行限流操作
	 *
	 * @param key          用于区分不同的限流器
	 * @param rateInterval 限流时间单位
	 * @param rate         限流单位时间内的最大令牌数
	 * @param permit       每次操作消耗的令牌数，不能超过 rate
	 */
	public void doRateLimit(String key, TimeModel rateInterval, Long rate, Long permit) {
		try {
			RateLimitUtils.doRateLimit(key, rateInterval, rate, permit);
			log.info("Rate limit succeeded for key: {}, rateInterval: {}, rate: {}, permit: {}",
					key, rateInterval, rate, permit);
		} catch (BusinessException e) {
			log.warn("Rate limit failed for key: {}, reason: {}", key, e.getMessage(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
			
		}
	}
	
	/**
	 * 执行限流并设置过期时间
	 *
	 * @param key          用于区分不同的限流器
	 * @param rateInterval 限流时间单位
	 * @param rate         限流单位时间内的最大令牌数
	 * @param permit       每次操作消耗的令牌数，不能超过 rate
	 * @param expire       限流器键值的过期时间
	 */
	public void doRateLimitAndExpire(String key, TimeModel rateInterval, Long rate, Long permit, TimeModel expire) {
		try {
			RateLimitUtils.doRateLimitAndExpire(key, rateInterval, rate, permit, expire);
			log.info("Rate limit with expiration succeeded for key: {}, rateInterval: {}, rate: {}, permit: {}, expire: {}",
					key, rateInterval, rate, permit, expire);
		} catch (BusinessException e) {
			log.warn("Rate limit with expiration failed for key: {}, reason: {}", key, e.getMessage(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
		}
	}
	
}
