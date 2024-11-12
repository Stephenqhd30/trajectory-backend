package com.stephen.trajectory.manager;

import com.stephen.trajectory.manager.oss.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * Cos 操作测试
 *
 * @author stephen qiu
 */
@SpringBootTest
class CosManagerTest {
	
	@Resource
	private CosManager cosManager;
	
	@Test
	void putObject() {
		cosManager.putObject("test", "test.json");
	}
}