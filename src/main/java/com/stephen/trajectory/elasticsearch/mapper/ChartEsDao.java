package com.stephen.trajectory.elasticsearch.mapper;

import com.stephen.trajectory.elasticsearch.modal.entity.ChartEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * 图表 ES 操作
 *
 * @author stephen qiu
 */
public interface ChartEsDao extends ElasticsearchRepository<ChartEsDTO, Long> {
	
}