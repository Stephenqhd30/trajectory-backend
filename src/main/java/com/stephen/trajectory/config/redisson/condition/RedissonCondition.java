package com.stephen.trajectory.config.redisson.condition;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Redisson配置条件类，仅需检查 redis.enabled 属性。
 *
 * @author stephen qiu
 */
public class RedissonCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String enabled = context.getEnvironment().getProperty("redis.enabled");
		return Boolean.parseBoolean(enabled);
	}
}
