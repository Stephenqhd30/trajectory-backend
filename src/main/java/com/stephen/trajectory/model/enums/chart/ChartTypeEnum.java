package com.stephen.trajectory.model.enums.chart;

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
public enum ChartTypeEnum {
	
	REVIEWING("柱状图", 0),
	PASS("折线图", 1),
	REJECT("饼图", 2);
	private final String text;
	
	private final Integer value;
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link ChartTypeEnum}
	 */
	public static ChartTypeEnum getEnumByValue(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ChartTypeEnum reviewStatusEnum : ChartTypeEnum.values()) {
			if (Objects.equals(reviewStatusEnum.value, value)) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
