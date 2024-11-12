package com.stephen.trajectory.elasticsearch.mapper;

import com.stephen.trajectory.elasticsearch.entity.UserEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * 帖子 ES 操作
 *
 * @author stephen qiu
 */
public interface UserEsDao extends ElasticsearchRepository<UserEsDTO, Long> {
	
}