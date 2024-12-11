package com.stephen.trajectory.manager.redis;

import com.stephen.trajectory.utils.redisson.rateLimit.model.TimeModel;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimiterManagerTest {
	
	@Resource
	private RedisLimiterManager redisLimiterManager;
	
	@Test
	void doRateLimit() throws InterruptedException {
		String userId = "1";
		for (int i = 0; i < 2; i++) {
			redisLimiterManager.doRateLimit(userId, new TimeModel(1L, TimeUnit.MINUTES), 10L, 10L);
			System.out.println("成功");
		}
	}
}