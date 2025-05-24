package com.stephen.trajectory.config.oss.cos.condition;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 腾讯云COS自定义配置条件
 *
 * @author stephen qiu
 */
public class CosCondition implements Condition {
	
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String property = context.getEnvironment().getProperty("cos.client.enable");
		return StringUtils.equals(Boolean.TRUE.toString(), property);
	}
}