package com.stephen.trajectory.elasticsearch.mapper;

import com.stephen.trajectory.elasticsearch.modal.entity.UserEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * 用户 ES 操作
 *
 * @author stephen qiu
 */
public interface UserEsDao extends ElasticsearchRepository<UserEsDTO, Long> {
	
}