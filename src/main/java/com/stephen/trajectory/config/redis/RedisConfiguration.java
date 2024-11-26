package com.stephen.trajectory.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.stephen.trajectory.config.redis.condition.RedisCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Redis的配置类
 * 用于配置 RedisTemplate，以便在 Spring 应用中使用 Redis
 *
 * @author: stephen qiu
 **/
@Configuration
@Conditional(RedisCondition.class)
@Slf4j
public class RedisConfiguration {
	
	@Resource
	private RedisConnectionFactory redisConnectionFactory;
	
	/**
	 * 配置 RedisTemplate 实例
	 *
	 * @return 配置好的 RedisTemplate 实例
	 */
	@Bean("redisTemplateBean")
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		// 设置 Redis 连接工厂
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		// 设置value的序列化方式json
		redisTemplate.setValueSerializer(redisSerializer());
		// 设置key序列化方式String
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		// 设置hash key序列化方式String
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		// 设置hash value序列化json
		redisTemplate.setHashValueSerializer(redisSerializer());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
	
	/**
	 * 配置 Redis 序列化器
	 *
	 * @return {@link   RedisSerializer<Object>}
	 */
	public RedisSerializer<Object> redisSerializer() {
		// 创建JSON序列化器
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		// 必须设置，否则无法序列化实体类对象
		objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName().split("\\$\\$")[0]);
	}
}
