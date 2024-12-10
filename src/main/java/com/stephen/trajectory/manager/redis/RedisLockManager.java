package com.stephen.trajectory.manager.redis;

import com.stephen.trajectory.config.redisson.condition.RedissonCondition;
import com.stephen.trajectory.utils.redisson.lock.LockUtils;
import com.stephen.trajectory.utils.redisson.lock.function.SuccessFunction;
import com.stephen.trajectory.utils.redisson.lock.function.VoidFunction;
import com.stephen.trajectory.utils.redisson.lock.model.TimeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁管理类
 * 提供基于 Redis + Redisson 实现的封装操作
 *
 * @author Stephen Qiu
 */
@Component
@Slf4j
@Conditional(RedissonCondition.class)
public class RedisLockManager {
	
	/**
	 * 执行无返回值的分布式锁操作
	 *
	 * @param key       锁的键
	 * @param eventFunc 获取锁后的操作
	 */
	public void executeLock(String key, VoidFunction eventFunc) {
		LockUtils.lockEvent(key, eventFunc);
	}
	
	/**
	 * 带自动释放时间的分布式锁操作
	 *
	 * @param key       锁的键
	 * @param leaseTime 锁的自动释放时间
	 * @param eventFunc 获取锁后的操作
	 */
	public void executeLock(String key, TimeModel leaseTime, VoidFunction eventFunc) {
		LockUtils.lockEvent(key, leaseTime, eventFunc);
	}
	
	/**
	 * 带有返回值的分布式锁操作（返回 boolean）
	 *
	 * @param key     锁的键
	 * @param success 成功获取锁的操作
	 * @return 是否成功获取锁
	 */
	public boolean executeLock(String key, SuccessFunction success) {
		return LockUtils.lockEvent(key, success);
	}
	
	/**
	 * 带有自定义等待时间的分布式锁操作（返回 boolean）
	 *
	 * @param key      锁的键
	 * @param waitTime 最大等待时间
	 * @param success  成功获取锁的操作
	 * @return 是否成功获取锁
	 */
	public boolean executeLock(String key, TimeModel waitTime, SuccessFunction success) {
		return LockUtils.lockEvent(key, waitTime, success);
	}
	
	/**
	 * 带有自定义返回值的分布式锁操作
	 *
	 * @param key     锁的键
	 * @param getLock 成功获取锁的操作
	 * @param getNone 获取锁失败的操作
	 * @param <T>     返回的类型
	 * @return 结果
	 */
	public <T> T executeLock(String key, Supplier<T> getLock, Supplier<T> getNone) {
		return LockUtils.lockEvent(key, getLock, getNone);
	}
	
	/**
	 * 带有自定义等待时间的分布式锁操作，支持返回自定义类型
	 *
	 * @param key      锁的键
	 * @param waitTime 最大等待时间
	 * @param getLock  成功获取锁的操作
	 * @param getNone  获取锁失败的操作
	 * @param <T>      返回的类型
	 * @return 结果
	 */
	public <T> T executeLock(String key, TimeModel waitTime, Supplier<T> getLock, Supplier<T> getNone) {
		return LockUtils.lockEvent(key, waitTime, getLock, getNone);
	}
	
	/**
	 * 带重试机制的分布式锁操作，支持返回自定义类型
	 *
	 * @param key           锁的键
	 * @param maxRetryTimes 最大重试次数
	 * @param retryInterval 每次重试的间隔时间（毫秒）
	 * @param getLock       成功获取锁的操作
	 * @param getNone       获取锁失败的操作
	 * @param <T>           返回的类型
	 * @return 结果
	 */
	public <T> T executeLockWithRetry(String key, int maxRetryTimes, long retryInterval, Supplier<T> getLock, Supplier<T> getNone) {
		log.info("Executing lock with retry: key={}, maxRetryTimes={}, retryInterval={}ms", key, maxRetryTimes, retryInterval);
		return LockUtils.lockEventWithRetry(key, maxRetryTimes, retryInterval, getLock, getNone);
	}
	
	/**
	 * 尝试获取锁并设置重试机制
	 *
	 * @param key           锁的键
	 * @param maxRetryTimes 最大重试次数
	 * @param waitTime      每次等待时间
	 * @return 是否成功获取锁
	 */
	public boolean tryLockWithRetry(String key, int maxRetryTimes, long waitTime) {
		log.info("Trying to acquire lock with retry: key={}, maxRetryTimes={}, waitTime={}ms", key, maxRetryTimes, waitTime);
		return LockUtils.tryLockWithRetry(key, maxRetryTimes, waitTime);
	}
	
	/**
	 * 使用看门狗机制保护锁，延长自动释放时间
	 *
	 * @param key      锁的键
	 * @param timeout  锁的超时时间
	 * @param timeUnit 超时单位
	 * @return 是否成功获取锁
	 */
	public boolean lockWithWatchdog(String key, long timeout, TimeUnit timeUnit) {
		log.info("Locking with watchdog: key={}, timeout={} {}", key, timeout, timeUnit);
		return LockUtils.lockWithWatchdog(key, timeout, timeUnit);
	}
}