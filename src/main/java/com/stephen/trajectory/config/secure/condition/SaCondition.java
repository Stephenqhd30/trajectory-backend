package com.stephen.trajectory.config.secure.condition;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * SaToken认证鉴权自定义配置条件
 *
 * @author stephen qiu
 */
public class SaCondition implements Condition {
	
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		String property = context.getEnvironment().getProperty("sa-token.enable-sa");
		return StringUtils.equals(Boolean.TRUE.toString(), property);
	}
	
}