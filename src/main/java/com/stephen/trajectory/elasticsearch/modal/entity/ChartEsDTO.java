package com.stephen.trajectory.elasticsearch.modal.entity;

import com.stephen.trajectory.model.entity.Chart;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * 用户 Elasticsearch DTO
 *
 * @author stephen qiu
 */
@Data
// todo 取消注释开启 ES（须先配置 ES）
@Document(indexName = "trajectory_chart", createIndex = false)
public class ChartEsDTO {
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	/**
	 * 用户ID
	 */
	@Id
	private Long id;
	
	/**
	 * 分析目标
	 */
	private String goal;
	
	/**
	 * 图表名称
	 */
	private String name;
	
	/**
	 * 图表数据
	 */
	private String chartData;
	
	/**
	 * 图表类型
	 */
	private String chartType;
	
	/**
	 * 生成的图表数据
	 */
	private String genChart;
	
	/**
	 * 生成的分析结论
	 */
	private String genResult;
	
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
	
	/**
	 * 创建时间
	 */
	@Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
	private Date updateTime;
	
	/**
	 * 是否删除
	 */
	private Integer isDelete;
	
	/**
	 * 对象转包装类
	 *
	 * @param chart chart
	 * @return {@link ChartEsDTO}
	 */
	public static ChartEsDTO objToDto(Chart chart) {
		if (chart == null) {
			return null;
		}
		ChartEsDTO chartEsDTO = new ChartEsDTO();
		BeanUtils.copyProperties(chart, chartEsDTO);
		return chartEsDTO;
	}
	
	/**
	 * 包装类转对象
	 *
	 * @param chartEsDTO chartEsDTO
	 * @return {@link Chart}
	 */
	public static Chart dtoToObj(ChartEsDTO chartEsDTO) {
		if (chartEsDTO == null) {
			return null;
		}
		Chart chart = new Chart();
		BeanUtils.copyProperties(chartEsDTO, chart);
		return chart;
	}
}
