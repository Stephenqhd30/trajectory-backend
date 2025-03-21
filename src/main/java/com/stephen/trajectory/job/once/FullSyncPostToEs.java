package com.stephen.trajectory.job.once;

import cn.hutool.core.collection.CollUtil;
import com.stephen.trajectory.elasticsearch.mapper.PostEsDao;
import com.stephen.trajectory.elasticsearch.modal.entity.PostEsDTO;
import com.stephen.trajectory.model.entity.Post;
import com.stephen.trajectory.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步帖子到 es
 *
 * @author stephen qiu
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {
	
	@Resource
	private PostService postService;
	
	@Resource
	private PostEsDao postEsDao;
	
	@Override
	public void run(String... args) {
		List<Post> postList = postService.list();
		if (CollUtil.isEmpty(postList)) {
			return;
		}
		List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
		final int pageSize = 500;
		int total = postEsDTOList.size();
		log.info("FullSyncPostToEs start, total {}", total);
		for (int i = 0; i < total; i += pageSize) {
			int end = Math.min(i + pageSize, total);
			log.info("sync from {} to {}", i, end);
			postEsDao.saveAll(postEsDTOList.subList(i, end));
		}
		log.info("FullSyncPostToEs end, total {}", total);
	}
}
