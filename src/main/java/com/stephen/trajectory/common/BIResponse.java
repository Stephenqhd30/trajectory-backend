package com.stephen.trajectory.common;

import lombok.Data;

/**
 * @author: stephen qiu
 * @create: 2024-07-01 16:34
 **/
@Data
public class BIResponse {
	
	/**
	 * 生成的图表数据
	 */
	private String genChart;
	
	
	/**
	 * 生成的分析结论
	 */
	private String genResult;
}
