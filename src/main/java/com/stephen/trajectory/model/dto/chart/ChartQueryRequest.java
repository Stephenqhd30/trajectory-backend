package com.stephen.trajectory.model.dto.chart;

import com.stephen.trajectory.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询图表信息请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * id
	 */
	private Long notId;
	
	/**
	 * 搜索关键字
	 */
	private String searchText;
	
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
	
	/**
	 * 图表状态(wait,running,succeed,failed)
	 */
	private String status;
	
	/**
	 * 执行信息
	 */
	private String executorMessage;
	
	/**
	 * 创建用户id
	 */
	private Long userId;
	
	
	private static final long serialVersionUID = 1L;
}