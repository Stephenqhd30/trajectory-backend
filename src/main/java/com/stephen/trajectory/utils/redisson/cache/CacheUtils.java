package com.stephen.trajectory.utils.redisson.cache;

import com.stephen.trajectory.config.bean.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;

/**
 * CacheUtils: 基于 Redisson 的 Redis 缓存工具类，提供通用的缓存管理功能。
 * <p>
 * 支持存取任意对象类型，包含多种数据结构，如字符串、列表、Map 和 Set 等。
 * </p>
 * 默认缓存时间为 5 分钟，可通过方法参数进行覆盖。
 *
 * @author stephen qiu
 */
@Slf4j
public class CacheUtils {
	
	/**
	 * 被封装的 Redisson 客户端对象
	 */
	private static final RedissonClient REDISSON_CLIENT = SpringContextHolder.getBean(RedissonClient.class);
	
	/**
	 * 默认缓存时间，单位：秒
	 */
	private static final Long DEFAULT_EXPIRED = 5 * 60L;
	
	/**
	 * Redis key 前缀
	 */
	private static final String REDIS_KEY_PREFIX = "";
	
	/**
	 * 读取缓存内容，自动序列化和反序列化
	 *
	 * @param key 缓存键
	 * @param <T> 返回值的类型
	 * @return 缓存值，或 null 如果键不存在
	 */
	public <T> T get(String key) {
		return getWithCodec(key, null);
	}
	
	/**
	 * 以字符串形式读取缓存内容
	 *
	 * @param key 缓存键
	 * @return 缓存中的字符串值，或 null 如果键不存在
	 */
	public String getString(String key) {
		return getWithCodec(key, StringCodec.INSTANCE);
	}
	
	/**
	 * 设置缓存内容，自动序列化并设置默认过期时间
	 *
	 * @param key   缓存键
	 * @param value 缓存值
	 * @param <T>   值的类型
	 */
	public <T> void put(String key, T value) {
		putWithExpiration(key, value, DEFAULT_EXPIRED);
	}
	
	/**
	 * 设置缓存内容，允许自定义过期时间
	 *
	 * @param key     缓存键
	 * @param value   缓存值
	 * @param expired 过期时间（秒），若为 0 或负数则使用默认过期时间
	 * @param <T>     值的类型
	 */
	public <T> void put(String key, T value, long expired) {
		putWithExpiration(key, value, expired);
	}
	
	/**
	 * 以字符串形式设置缓存内容
	 *
	 * @param key   缓存键
	 * @param value 缓存值
	 */
	public void putString(String key, String value) {
		putWithExpiration(key, value, DEFAULT_EXPIRED, StringCodec.INSTANCE);
	}
	
	/**
	 * 以字符串形式设置缓存内容，允许自定义过期时间
	 *
	 * @param key     缓存键
	 * @param value   缓存值
	 * @param expired 过期时间（秒），若为 0 或负数则使用默认过期时间
	 */
	public void putString(String key, String value, long expired) {
		putWithExpiration(key, value, expired, StringCodec.INSTANCE);
	}
	
	/**
	 * 删除缓存项
	 *
	 * @param key 缓存键
	 */
	public void remove(String key) {
		REDISSON_CLIENT.getBucket(prefixedKey(key)).delete();
	}
	
	/**
	 * 检查缓存项是否存在
	 *
	 * @param key 缓存键
	 * @return true 如果缓存项存在；否则 false
	 */
	public boolean exists(String key) {
		return REDISSON_CLIENT.getBucket(prefixedKey(key)).isExists();
	}
	
	/**
	 * 封装通用的缓存写入方法，允许指定过期时间
	 *
	 * @param key     缓存键
	 * @param value   缓存值
	 * @param expired 过期时间（秒），为 0 或负数时使用默认过期时间
	 * @param <T>     值的类型
	 */
	private <T> void putWithExpiration(String key, T value, long expired) {
		putWithExpiration(key, value, expired, null);
	}
	
	/**
	 * 封装通用的缓存写入方法，允许指定过期时间和自定义编解码器
	 *
	 * @param key     缓存键
	 * @param value   缓存值
	 * @param expired 过期时间（秒），为 0 或负数时使用默认过期时间
	 * @param codec   编解码器（用于指定数据的序列化方式）
	 * @param <T>     值的类型
	 */
	private <T> void putWithExpiration(String key, T value, long expired, StringCodec codec) {
		RBucket<T> bucket = codec == null
				? REDISSON_CLIENT.getBucket(prefixedKey(key))
				: REDISSON_CLIENT.getBucket(prefixedKey(key), codec);
		bucket.set(value, expired <= 0 ? DEFAULT_EXPIRED : expired, TimeUnit.SECONDS);
	}
	
	/**
	 * 封装通用的缓存读取方法，允许自定义编解码器
	 *
	 * @param key   缓存键
	 * @param codec 编解码器
	 * @return 缓存值，或 null 如果键不存在
	 */
	private <T> T getWithCodec(String key, StringCodec codec) {
		RBucket<T> bucket = codec == null
				? REDISSON_CLIENT.getBucket(prefixedKey(key))
				: REDISSON_CLIENT.getBucket(prefixedKey(key), codec);
		return bucket.get();
	}
	
	/**
	 * 为键添加 Redis 前缀
	 *
	 * @param key 原始缓存键
	 * @return 添加前缀后的缓存键
	 */
	private String prefixedKey(String key) {
		return REDIS_KEY_PREFIX + key;
	}
}