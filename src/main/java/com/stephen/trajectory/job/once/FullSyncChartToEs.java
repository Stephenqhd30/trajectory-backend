package com.stephen.trajectory.job.once;

import cn.hutool.core.collection.CollUtil;
import com.stephen.trajectory.elasticsearch.mapper.ChartEsDao;
import com.stephen.trajectory.elasticsearch.modal.entity.ChartEsDTO;
import com.stephen.trajectory.model.entity.Chart;
import com.stephen.trajectory.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步图表到 es
 *
 * @author stephen qiu
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class FullSyncChartToEs implements CommandLineRunner {
	
	@Resource
	private ChartService chartService;
	
	@Resource
	private ChartEsDao chartEsDao;
	
	@Override
	public void run(String... args) {
		List<Chart> chartList = chartService.list();
		if (CollUtil.isEmpty(chartList)) {
			return;
		}
		List<ChartEsDTO> chartEsDTOList = chartList.stream().map(ChartEsDTO::objToDto).collect(Collectors.toList());
		final int pageSize = 500;
		int total = chartEsDTOList.size();
		log.info("FullSyncChartToEs start, total {}", total);
		for (int i = 0; i < total; i += pageSize) {
			int end = Math.min(i + pageSize, total);
			log.info("sync from {} to {}", i, end);
			chartEsDao.saveAll(chartEsDTOList.subList(i, end));
		}
		log.info("FullSyncChartToEs end, total {}", total);
	}
}
