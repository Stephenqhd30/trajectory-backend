package com.stephen.trajectory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stephen.trajectory.model.entity.Chart;

import java.util.Date;
import java.util.List;

/**
 * @author stephen qiu
 * @description 针对表【chart(图表信息)】的数据库操作Mapper
 * @createDate 2024-11-12 16:03:22
 * @Entity com.stephen.trajectory.model.entity.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {
	
	/**
	 * 查询图表列表（包括已被删除的数据）
	 */
	List<Chart> listChartWithDelete(Date minUpdateTime);
}




