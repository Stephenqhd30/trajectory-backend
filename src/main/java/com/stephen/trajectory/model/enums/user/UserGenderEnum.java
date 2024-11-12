package com.stephen.trajectory.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stephen qiu
 */

@Getter
@AllArgsConstructor
public enum UserGenderEnum {
	
	
	MALE("男", 0),
	FEMALE("女", 1),
	SECURITY("保密", 2);
	
	private final String text;
	
	private final Integer value;
	
	
	/**
	 * 获取值列表
	 *
	 * @return {@link List<Integer>}
	 */
	public static List<Integer> getValues() {
		return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link UserGenderEnum}
	 */
	public static UserGenderEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (UserGenderEnum anEnum : UserGenderEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
	
}
