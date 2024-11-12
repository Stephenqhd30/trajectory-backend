package com.stephen.trajectory.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author stephen qiu
 */
@Data
public class GenChartByAiRequest implements Serializable {
	
	private static final long serialVersionUID = -1193592573887027535L;
	
	
	/**
	 * 分析目标
	 */
	private String goal;
	
	/**
	 * 图表名称
	 */
	private String name;
	
	/**
	 * 图表类型
	 */
	private String chartType;
}