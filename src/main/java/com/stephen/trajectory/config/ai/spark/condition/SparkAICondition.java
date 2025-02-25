package com.stephen.trajectory.config.ai.spark.condition;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否开启星火AI
 *
 * @author: stephen qiu
 **/
public class SparkAICondition implements Condition {
	
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String property = context.getEnvironment().getProperty("xunfei.enabled");
		return StringUtils.equals(Boolean.TRUE.toString(), property);
	}
}
