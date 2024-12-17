package com.stephen.trajectory.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stephen.trajectory.model.dto.chart.ChartQueryRequest;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.model.vo.ChartVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * 图表信息服务
 *
 * @author stephen qiu
 */
public interface ChartService extends IService<Chart> {
	
	/**
	 * 校验数据
	 *
	 * @param chart chart
	 * @param add   对创建的数据进行校验
	 */
	void validChart(Chart chart, boolean add);
	
	/**
	 * 获取查询条件
	 *
	 * @param chartQueryRequest chartQueryRequest
	 * @return {@link QueryWrapper<Chart>}
	 */
	QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);
	
	/**
	 * 获取图表信息封装
	 *
	 * @param chart   chart
	 * @param request request
	 * @return {@link ChartVO}
	 */
	ChartVO getChartVO(Chart chart, HttpServletRequest request);
	
	/**
	 * 分页获取图表信息封装
	 *
	 * @param chartPage chartPage
	 * @param request   request
	 * @return {@link Page
	 * <ChartVO>}
	 */
	Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request);
	
	/**
	 * 构造用户输入内容
	 *
	 * @param chart      图表对象
	 * @param excelToCsv 转换为 Csv 的数据
	 * @return 构造好的用户输入字符串
	 */
	String constructUserInput(Chart chart, String excelToCsv);
	
	/**
	 * 限流
	 *
	 * @param loginUser loginUser
	 */
	void doRateLimit(User loginUser);
	
	/**
	 * AI 服务调用失败处理
	 *
	 * @param chartId         chartId
	 * @param executorMessage executorMessage
	 */
	void executorError(Long chartId, String executorMessage);
}