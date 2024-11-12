package com.stephen.trajectory.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 审核状态枚举类
 * 审核状态：0-待审核, 1-通过, 2-拒绝
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {
	
	REVIEWING("待审核", 0),
	PASS("通过", 1),
	REJECT("拒绝", 2);
	private final String text;
	
	private final Integer value;
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link ReviewStatusEnum}
	 */
	public static ReviewStatusEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ReviewStatusEnum reviewStatusEnum : ReviewStatusEnum.values()) {
			if (Objects.equals(reviewStatusEnum.value, value)) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
