package com.stephen.trajectory.mapper;

import com.stephen.trajectory.model.entity.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作测试
 *
 * @author stephen qiu
 */
@SpringBootTest
class PostMapperTest {
	
	@Resource
	private PostMapper postMapper;
	
	@Test
	void listPostWithDelete() {
		List<Post> postList = postMapper.listPostWithDelete(new Date());
		Assertions.assertNotNull(postList);
	}
}