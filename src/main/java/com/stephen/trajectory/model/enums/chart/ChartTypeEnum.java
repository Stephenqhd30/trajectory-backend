package com.stephen.trajectory.model.enums.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 图表类型枚举类
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum ChartTypeEnum {
	Line("折线图", "折线图"),
	Bar("'柱状图'", "柱状图"),
	Pie("饼图", "饼图"),
	Scatter("散点图", "散点图"),
	KLineChart("K线图", "K线图");
	private final String text;
	
	private final String value;
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link ChartTypeEnum}
	 */
	public static ChartTypeEnum getEnumByValue(String value) {
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
