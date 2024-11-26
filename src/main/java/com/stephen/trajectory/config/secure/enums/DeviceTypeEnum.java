package com.stephen.trajectory.config.secure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 设备枚举类
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {
	
	PC("电脑", "PC"),
	MOBILE("移动端", "MOBILE"),
	TABLET("平板", "TABLET"),
	MINI("小程序", "MINI"),
	UNKNOWN("未知设备", "UNKNOWN");
	/**
	 * 文本
	 */
	private final String text;
	
	/**
	 * 值
	 */
	private final String value;
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link DeviceTypeEnum}
	 */
	public static DeviceTypeEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (DeviceTypeEnum reviewStatusEnum : DeviceTypeEnum.values()) {
			if (Objects.equals(reviewStatusEnum.value, value)) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
