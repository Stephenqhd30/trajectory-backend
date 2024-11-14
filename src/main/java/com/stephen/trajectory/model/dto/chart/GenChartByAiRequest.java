package com.stephen.trajectory.model.dto.chart;

import com.stephen.trajectory.model.dto.file.UploadFileRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GenChartByAiRequest extends UploadFileRequest implements Serializable {
	
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