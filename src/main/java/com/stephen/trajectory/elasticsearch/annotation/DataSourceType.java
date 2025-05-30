package com.stephen.trajectory.elasticsearch.annotation;

import com.stephen.trajectory.elasticsearch.modal.enums.SearchTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据类型注解
 *
 * @author stephen qiu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataSourceType {
	SearchTypeEnum value();
}
