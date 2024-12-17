package com.stephen.trajectory.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.stephen.trajectory.elasticsearch.mapper.ChartEsDao;
import com.stephen.trajectory.elasticsearch.modal.entity.ChartEsDTO;
import com.stephen.trajectory.mapper.ChartMapper;
import com.stephen.trajectory.model.entity.Chart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步图表到 es
 *
 * @author stephen qiu
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncChartToEs {
	
	@Resource
	private ChartMapper chartMapper;
	
	@Resource
	private ChartEsDao chartEsDao;
	
	/**
	 * 每3分钟执行一次
	 */
	@Scheduled(fixedRate = 3 * 60 * 1000)
	public void run() {
		// 查询近 5 分钟内的数据
		Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
		List<Chart> chartList = chartMapper.listChartWithDelete(fiveMinutesAgoDate);
		if (CollUtil.isEmpty(chartList)) {
			log.info("no inc chart");
			return;
		}
		List<ChartEsDTO> chartEsDTOList = chartList.stream()
				.map(ChartEsDTO::objToDto)
				.collect(Collectors.toList());
		final int pageSize = 500;
		int total = chartEsDTOList.size();
		log.info("IncSyncChartToEs start, total {}", total);
		for (int i = 0; i < total; i += pageSize) {
			int end = Math.min(i + pageSize, total);
			log.info("sync from {} to {}", i, end);
			chartEsDao.saveAll(chartEsDTOList.subList(i, end));
		}
		log.info("IncSyncChartToEs end, total {}", total);
	}
}
