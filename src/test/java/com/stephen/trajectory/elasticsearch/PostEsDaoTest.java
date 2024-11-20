package com.stephen.trajectory.elasticsearch;

import com.stephen.trajectory.elasticsearch.entity.PostEsDTO;
import com.stephen.trajectory.elasticsearch.mapper.PostEsDao;
import com.stephen.trajectory.elasticsearch.service.PostEsService;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import com.stephen.trajectory.model.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 帖子 ES 操作测试
 *
 * @author stephen qiu
 */
@SpringBootTest
public class PostEsDaoTest {
	
	@Resource
	private PostEsDao postEsDao;
	
	@Resource
	private PostEsService postEsService;
	
	// @Test
	// void test() {
	// 	PostQueryRequest postQueryRequest = new PostQueryRequest();
	// 	com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
	// 			postEsService.searchPostFromEs(postQueryRequest);
	// 	System.out.println(page);
	// }
	//
	// @Test
	// void testSelect() {
	// 	System.out.println(postEsDao.count());
	// 	Page<PostEsDTO> PostPage = postEsDao.findAll(
	// 			PageRequest.of(0, 5, Sort.by("createTime")));
	// 	List<PostEsDTO> postList = PostPage.getContent();
	// 	System.out.println(postList);
	// }
	//
	// @Test
	// void testAdd() {
	// 	PostEsDTO postEsDTO = new PostEsDTO();
	// 	postEsDTO.setId(1L);
	// 	postEsDTO.setTitle("test");
	// 	postEsDTO.setContent("test");
	// 	postEsDTO.setTags(Arrays.asList("java", "python"));
	// 	postEsDTO.setThumbNum(1);
	// 	postEsDTO.setFavourNum(1);
	// 	postEsDTO.setUserId(1L);
	// 	postEsDTO.setCreateTime(new Date());
	// 	postEsDTO.setUpdateTime(new Date());
	// 	postEsDTO.setIsDelete(0);
	// 	postEsDao.save(postEsDTO);
	// 	System.out.println(postEsDTO.getId());
	// }
	//
	// @Test
	// void testFindById() {
	// 	Optional<PostEsDTO> postEsDTO = postEsDao.findById(1L);
	// 	System.out.println(postEsDTO);
	// }
	//
	// @Test
	// void testCount() {
	// 	System.out.println(postEsDao.count());
	// }
	//
	// @Test
	// void testFindByCategory() {
	// 	List<PostEsDTO> postEsDaoTestList = postEsDao.findByUserId(1L);
	// 	System.out.println(postEsDaoTestList);
	// }
}
