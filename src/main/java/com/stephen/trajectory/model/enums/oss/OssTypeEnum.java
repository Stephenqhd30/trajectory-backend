package com.stephen.trajectory.model.enums.oss;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象存储类型
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum OssTypeEnum {
	
	/**
	 * MinIO
	 */
	MINIO("MinIO", "minio"),
	
	/**
	 * 阿里云OSS
	 */
	ALI_OSS("阿里云OSS", "condition"),
	
	/**
	 * 腾讯云COS
	 */
	COS("腾讯云COS", "cos");
	
	private final String text;
	
	private final String value;
	
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
	 * @return {@link OssTypeEnum}
	 */
	public static OssTypeEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (OssTypeEnum anEnum : OssTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
	
}
