package com.stephen.trajectory.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.stephen.trajectory.elasticsearch.mapper.UserEsDao;
import com.stephen.trajectory.elasticsearch.modal.entity.UserEsDTO;
import com.stephen.trajectory.mapper.UserMapper;
import com.stephen.trajectory.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步用户到 es
 *
 * @author stephen qiu
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncUserToEs {
	
	@Resource
	private UserMapper userMapper;
	
	@Resource
	private UserEsDao userEsDao;
	
	/**
	 * 每3分钟执行一次
	 */
	@Scheduled(fixedRate = 60 * 1000)
	public void run() {
		// 查询近 5 分钟内的数据
		Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
		List<User> userList = userMapper.listUserWithDelete(fiveMinutesAgoDate);
		if (CollUtil.isEmpty(userList)) {
			log.info("no inc user");
			return;
		}
		List<UserEsDTO> userEsDTOList = userList.stream()
				.map(UserEsDTO::objToDto)
				.collect(Collectors.toList());
		final int pageSize = 500;
		int total = userEsDTOList.size();
		log.info("IncSyncUserToEs start, total {}", total);
		for (int i = 0; i < total; i += pageSize) {
			int end = Math.min(i + pageSize, total);
			log.info("sync from {} to {}", i, end);
			userEsDao.saveAll(userEsDTOList.subList(i, end));
		}
		log.info("IncSyncUserToEs end, total {}", total);
	}
}
