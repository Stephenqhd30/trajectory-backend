package com.stephen.trajectory.config.ai.deepseek.condition;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 是否开启DeepSeekAI
 *
 * @author: stephen qiu
 **/
public class DeepSeekAICondition implements Condition {
	
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String property = context.getEnvironment().getProperty("deepseek.enabled");
		return StringUtils.equals(Boolean.TRUE.toString(), property);
	}
}
