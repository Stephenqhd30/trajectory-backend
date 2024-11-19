package com.stephen.trajectory.utils.redisson.rateLimit;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.bean.SpringContextHolder;
import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.utils.redisson.KeyPrefixConstants;
import com.stephen.trajectory.utils.redisson.rateLimit.model.TimeModel;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * 限流工具类，提供基于 Redisson 的令牌桶限流功能
 *
 * @author stephen qiu
 */
@Component
@Conditional(RedissonCondition.class)
public class RateLimitUtils {
	
	// Redisson 客户端
	private static final RedissonClient REDISSON_CLIENT = SpringContextHolder.getBean(RedissonClient.class);
	
	/**
	 * 初始化，删除 Redis 中的限流键值对
	 */
	@PostConstruct
	private void init() {
		clearRateLimitKeys();
	}
	
	/**
	 * 程序销毁时，清除限流键值对
	 */
	@PreDestroy
	private void cleanup() {
		clearRateLimitKeys();
	}
	
	/**
	 * 删除 Redis 中的限流键值对
	 */
	private void clearRateLimitKeys() {
		REDISSON_CLIENT.getKeys().deleteByPattern(KeyPrefixConstants.RATE_LIMIT_UTILS_PREFIX + "*");
	}
	
	/**
	 * 执行限流操作
	 *
	 * @param key          区分不同限流器的唯一标识
	 * @param rateInterval 限流时间单位
	 * @param rate         限流单位时间内的最大令牌数
	 * @param permit       每次操作消耗的令牌数，不能超过 rate
	 */
	public static void doRateLimit(String key, TimeModel rateInterval, Long rate, Long permit) {
		// 校验令牌数是否合法
		if (rate < permit) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "令牌数不足");
		}
		
		// 获取限流器并设置限流参数
		RRateLimiter rateLimiter = REDISSON_CLIENT.getRateLimiter(KeyPrefixConstants.RATE_LIMIT_UTILS_PREFIX + key);
		rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval.toMillis(), RateIntervalUnit.MILLISECONDS);
		
		// 尝试获取令牌
		if (!rateLimiter.tryAcquire(permit)) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作过于频繁");
		}
	}
	
	/**
	 * 执行限流并设置过期时间
	 *
	 * @param key          区分不同限流器的唯一标识
	 * @param rateInterval 限流时间单位
	 * @param rate         限流单位时间内的最大令牌数
	 * @param permit       每次操作消耗的令牌数，不能超过 rate
	 * @param expire       限流器键值的过期时间
	 */
	public static void doRateLimitAndExpire(String key, TimeModel rateInterval, Long rate, Long permit, TimeModel expire) {
		// 执行限流操作
		doRateLimit(key, rateInterval, rate, permit);
		String baseKey = KeyPrefixConstants.RATE_LIMIT_UTILS_PREFIX + key;
		
		// 计算过期时间，确保时间大于限流单位时间
		long expireMillis = Math.max(expire.toMillis(), rateInterval.toMillis());
		// 为限流器设置过期时间
		REDISSON_CLIENT.getBucket(baseKey).expire(expireMillis, TimeUnit.MILLISECONDS);
	}
}
