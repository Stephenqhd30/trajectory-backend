package com.stephen.trajectory.model.enums.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 图表状态枚举类
 * 图表状态(wait,running,succeed,failed)
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum ChartStatusEnum {
	
	WAIT("等待中", "wait"),
	RUNNING("执行中", "running"),
	SUCCEED("执行成功", "succeed"),
	FAILED("执行失败", "failed");
	
	private final String text;
	
	private final String value;
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link ChartStatusEnum}
	 */
	public static ChartStatusEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (ChartStatusEnum reviewStatusEnum : ChartStatusEnum.values()) {
			if (Objects.equals(reviewStatusEnum.value, value)) {
				return reviewStatusEnum;
			}
		}
		return null;
	}
}
