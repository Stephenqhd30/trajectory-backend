package com.stephen.trajectory.utils.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.stephen.trajectory.config.bean.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 本地缓存工具类，提供对 Caffeine 缓存的基本操作
 *
 * @author stephen qiu
 */
@Slf4j
public class LocalCacheUtils {
	
	// 被封装的 Caffeine 缓存操作类
	@SuppressWarnings("unchecked")
	private static final Cache<String, Object> CAFFEINE_CLIENT = SpringContextHolder.getBean("localCache", Cache.class);
	
	/**
	 * 设置缓存
	 *
	 * @param key   缓存键
	 * @param value 缓存值
	 */
	public static void put(String key, Object value) {
		CAFFEINE_CLIENT.put(key, value);
	}
	
	/**
	 * 批量设置缓存
	 *
	 * @param keyAndValues 缓存键值对
	 */
	public static void putAll(Map<String, Object> keyAndValues) {
		CAFFEINE_CLIENT.putAll(keyAndValues);
	}
	
	/**
	 * 获取缓存值
	 *
	 * @param key 缓存键
	 * @return 返回对应的缓存值，如果不存在则返回 null
	 */
	public static Object get(String key) {
		return CAFFEINE_CLIENT.getIfPresent(key);
	}
	
	/**
	 * 批量获取缓存值
	 *
	 * @param keys 缓存键集合
	 * @return 返回包含所有存在键的缓存值的映射
	 */
	public static Map<String, Object> getAll(Iterable<String> keys) {
		return CAFFEINE_CLIENT.getAllPresent(keys);
	}
	
	/**
	 * 删除指定的缓存
	 *
	 * @param key 缓存键
	 */
	public static void delete(String key) {
		CAFFEINE_CLIENT.invalidate(key);
	}
	
	/**
	 * 批量删除缓存
	 *
	 * @param keys 缓存键集合
	 */
	public static void delete(Iterable<String> keys) {
		CAFFEINE_CLIENT.invalidateAll(keys);
	}
	
	/**
	 * 清空所有缓存
	 */
	public static void clear() {
		CAFFEINE_CLIENT.invalidateAll();
	}
}
