package com.stephen.trajectory.model.vo;

import com.stephen.trajectory.model.entity.Chart;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 图表信息视图
 *
 * @author stephen
 */
@Data
public class ChartVO implements Serializable {
	
	/**
	 * id
	 */
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
	 * 创建用户id
	 */
	private Long userId;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	/**
	 * 创建用户信息
	 */
	private UserVO userVO;
	
	/**
	 * 封装类转对象
	 *
	 * @param chartVO chartVO
	 * @return {@link Chart}
	 */
	public static Chart voToObj(ChartVO chartVO) {
		if (chartVO == null) {
			return null;
		}
		Chart chart = new Chart();
		BeanUtils.copyProperties(chartVO, chart);
		return chart;
	}
	
	/**
	 * 对象转封装类
	 *
	 * @param chart chart
	 * @return {@link ChartVO}
	 */
	public static ChartVO objToVo(Chart chart) {
		if (chart == null) {
			return null;
		}
		ChartVO chartVO = new ChartVO();
		BeanUtils.copyProperties(chart, chartVO);
		return chartVO;
	}
}
