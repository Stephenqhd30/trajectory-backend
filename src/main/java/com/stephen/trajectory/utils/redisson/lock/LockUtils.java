package com.stephen.trajectory.utils.redisson.lock;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.bean.SpringContextHolder;
import com.stephen.trajectory.utils.redisson.KeyPrefixConstants;
import com.stephen.trajectory.utils.redisson.lock.function.SuccessFunction;
import com.stephen.trajectory.utils.redisson.lock.function.VoidFunction;
import com.stephen.trajectory.utils.redisson.lock.model.TimeModel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 该工具类封装了 Redisson 提供的分布式锁功能，主要提供以下几种锁的使用方式：
 * 1. 无返回值的锁操作
 * 2. 返回 boolean 的锁操作
 * 3. 返回自定义类型的锁操作
 * 使用前需了解 Redisson 的锁机制及其看门狗机制。
 *
 * @author Stephen Qiu
 */
@Slf4j
public class LockUtils {
	
	private static final RedissonClient REDISSON_CLIENT = SpringContextHolder.getBean(RedissonClient.class);
	
	/**
	 * 无返回值的锁操作
	 *
	 * @param key       锁的键值
	 * @param eventFunc 获取锁后的操作
	 */
	public static void lockEvent(String key, VoidFunction eventFunc) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		try {
			lock.lock();
			eventFunc.method();
		} finally {
			unlockIfLocked(lock);
		}
	}
	
	/**
	 * 带有自动释放时间的锁操作
	 *
	 * @param key       锁的键值
	 * @param leaseTime 自动释放时间
	 * @param eventFunc 获取锁后的操作
	 */
	public static void lockEvent(String key, TimeModel leaseTime, VoidFunction eventFunc) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		try {
			lock.lock(leaseTime.getTime(), leaseTime.getUnit());
			eventFunc.method();
		} finally {
			unlockIfLocked(lock);
		}
	}
	
	/**
	 * 返回 boolean 的锁操作
	 *
	 * @param key     锁的键值
	 * @param success 获取锁成功的操作
	 * @return 返回结果
	 */
	public static boolean lockEvent(String key, SuccessFunction success) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		try {
			lockResult = lock.tryLock();
			if (lockResult) {
				success.method();
			}
		} finally {
			unlockIfLocked(lock, lockResult);
		}
		return lockResult;
	}
	
	/**
	 * 带有自定义等待时间的锁操作
	 *
	 * @param key      锁的键值
	 * @param waitTime 最大等待时间
	 * @param success  获取锁成功的操作
	 * @return 返回结果
	 */
	public static boolean lockEvent(String key, TimeModel waitTime, SuccessFunction success) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		try {
			lockResult = lock.tryLock(waitTime.getTime(), waitTime.getUnit());
			if (lockResult) {
				success.method();
			}
		} catch (InterruptedException e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		} finally {
			unlockIfLocked(lock, lockResult);
		}
		return lockResult;
	}
	
	/**
	 * 返回自定义类型的锁操作
	 *
	 * @param key     锁的键值
	 * @param getLock 获取锁成功的操作
	 * @param getNone 获取锁失败的操作
	 * @param <T>     返回的类型泛型
	 * @return 返回结果
	 */
	public static <T> T lockEvent(String key, Supplier<T> getLock, Supplier<T> getNone) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		try {
			lockResult = lock.tryLock();
			return lockResult ? getLock.get() : getNone.get();
		} finally {
			unlockIfLocked(lock, lockResult);
		}
	}
	
	/**
	 * 封装的解锁逻辑，确保在持锁线程中正确释放锁
	 *
	 * @param lock         RLock 对象
	 * @param shouldUnlock 是否需要解锁
	 */
	private static void unlockIfLocked(RLock lock, boolean shouldUnlock) {
		if (shouldUnlock && lock.isLocked()) {
			lock.unlock();
		}
	}
	
	/**
	 * 封装的解锁逻辑，仅接受 RLock 对象
	 *
	 * @param lock RLock 对象
	 */
	private static void unlockIfLocked(RLock lock) {
		if (lock.isLocked()) {
			lock.unlock();
		}
	}
	
	/**
	 * 尝试获取锁并设置重试机制
	 *
	 * @param key           锁的键值
	 * @param maxRetryTimes 最大重试次数
	 * @param waitTime      等待时间
	 * @return 是否成功获取锁
	 */
	public static boolean tryLockWithRetry(String key, int maxRetryTimes, long waitTime) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		for (int i = 0; i < maxRetryTimes; i++) {
			try {
				lockResult = lock.tryLock(waitTime, TimeUnit.MILLISECONDS);
				if (lockResult) {
					break;
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR);
			}
		}
		return lockResult;
	}
	
	/**
	 * 使用看门狗机制保护长时间任务，延长锁的自动释放时间
	 *
	 * @param key      锁的键值
	 * @param timeout  锁的超时
	 * @param timeUnit 超时单位
	 * @return 获取到锁则返回 true，否则返回 false
	 */
	public static boolean lockWithWatchdog(String key, long timeout, TimeUnit timeUnit) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		try {
			// 通过加锁超时设置锁超时
			boolean lockResult = lock.tryLock(timeout, timeUnit);
			if (lockResult) {
				// 使用看门狗延长锁的持有时间
				lock.lockAsync(timeout, timeUnit);
			}
			return lockResult;
		} catch (InterruptedException e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
	}
	
	/**
	 * 尝试获取锁带有重试机制，返回自定义类型。
	 *
	 * @param key           锁的键值
	 * @param maxRetryTimes 最大重试次数
	 * @param retryInterval 每次重试间隔时间（毫秒）
	 * @param getLock       获取锁成功的操作
	 * @param getNone       获取锁失败的操作
	 * @param <T>           返回的类型泛型
	 * @return 返回结果
	 */
	public static <T> T lockEventWithRetry(String key, int maxRetryTimes, long retryInterval, Supplier<T> getLock, Supplier<T> getNone) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		int retryCount = 0;
		
		while (retryCount < maxRetryTimes) {
			try {
				lockResult = lock.tryLock(retryInterval, TimeUnit.MILLISECONDS);
				if (lockResult) {
					return getLock.get();
				}
				retryCount++;
				Thread.sleep(retryInterval);
			} catch (InterruptedException e) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "Lock interrupted during retries");
			}
		}
		
		return getNone.get();
	}
	
	/**
	 * 返回自定义类型的锁操作，支持设置等待时间。
	 *
	 * @param key      锁的键值
	 * @param waitTime 最大等待时间
	 * @param getLock  获取锁成功的操作
	 * @param getNone  获取锁失败的操作
	 * @param <T>      返回的类型泛型
	 * @return 返回结果
	 */
	public static <T> T lockEvent(String key, TimeModel waitTime, Supplier<T> getLock, Supplier<T> getNone) {
		RLock lock = REDISSON_CLIENT.getLock(KeyPrefixConstants.LOCK_PREFIX + key);
		boolean lockResult = false;
		try {
			lockResult = lock.tryLock(waitTime.getTime(), waitTime.getUnit());
			return lockResult ? getLock.get() : getNone.get();
		} catch (InterruptedException e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "Lock interrupted while waiting");
		} finally {
			unlockIfLocked(lock, lockResult);
		}
	}
	
}
