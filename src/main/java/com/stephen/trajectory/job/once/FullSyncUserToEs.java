package com.stephen.trajectory.job.once;

import cn.hutool.core.collection.CollUtil;
import com.stephen.trajectory.elasticsearch.entity.UserEsDTO;
import com.stephen.trajectory.elasticsearch.mapper.UserEsDao;
import com.stephen.trajectory.model.entity.User;
import com.stephen.trajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步帖子到 es
 *
 * @author stephen qiu
 */
// todo 取消注释开启任务
// @Component
@Slf4j
public class FullSyncUserToEs implements CommandLineRunner {
	
	@Resource
	private UserService userService;
	
	@Resource
	private UserEsDao userEsDao;
	
	@Override
	public void run(String... args) {
		List<User> userList = userService.list();
		if (CollUtil.isEmpty(userList)) {
			return;
		}
		List<UserEsDTO> userEsDTOList = userList.stream().map(UserEsDTO::objToDto).collect(Collectors.toList());
		final int pageSize = 500;
		int total = userEsDTOList.size();
		log.info("FullSyncUserToEs start, total {}", total);
		for (int i = 0; i < total; i += pageSize) {
			int end = Math.min(i + pageSize, total);
			log.info("sync from {} to {}", i, end);
			userEsDao.saveAll(userEsDTOList.subList(i, end));
		}
		log.info("FullSyncUserToEs end, total {}", total);
	}
}
