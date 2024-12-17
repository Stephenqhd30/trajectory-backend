package com.stephen.trajectory.elasticsearch.modal.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索类型枚举
 *
 * @author stephen qiu
 */
@Getter
public enum SearchTypeEnum {
	
	POST("帖子", "post"),
	CHART("图表", "chart"),
	CONSUMER("用户", "consumer");
	
	private final String text;
	
	private final String value;
	
	SearchTypeEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}
	
	/**
	 * 获取值列表
	 *
	 * @return {@link List<String>}
	 */
	public static List<String> getValues() {
		return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link SearchTypeEnum}
	 */
	public static SearchTypeEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
	
}
