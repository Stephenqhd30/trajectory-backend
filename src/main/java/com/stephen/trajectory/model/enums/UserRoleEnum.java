package com.stephen.trajectory.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {
	
	USER("用户", "user"),
	ADMIN("管理员", "admin"),
	BAN("被封号", "ban");
	
	private final String text;
	
	private final String value;
	
	/**
	 * 获取值列表
	 *
	 * @return List<String>
	 */
	public static List<String> getValues() {
		return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link UserRoleEnum}
	 */
	public static UserRoleEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (UserRoleEnum anEnum : UserRoleEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
	
}
