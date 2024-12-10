package com.stephen.trajectory.config.thread;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;


/**
 * 自定义线程池配置
 *
 * @author stephen qiu
 */
@Slf4j
@Configuration
public class ThreadPoolExecutorConfiguration {
	
	@Bean
	public ThreadPoolExecutor threadPoolExecutor() {
		// 核心线程数：CPU核心数的2倍
		int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
		// 最大线程数：核心线程数的2倍
		int maxPoolSize = corePoolSize * 2;
		// 非核心线程空闲存活时间
		long keepAliveTime = 60L;
		// 阻塞队列容量
		int queueCapacity = 500;
		
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
				corePoolSize,
				maxPoolSize,
				keepAliveTime,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(queueCapacity),
				new ThreadFactory() {
					private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
					private int threadCount = 1;
					
					@Override
					public Thread newThread(@NotNull Runnable r) {
						Thread thread = defaultFactory.newThread(r);
						thread.setName("custom-thread-pool-" + threadCount++);
						return thread;
					}
				},
				(r, executor) -> log.warn("Task rejected! Queue is full and no available threads.")
		);
		
		// 添加自定义监控日志
		ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);
		monitor.scheduleAtFixedRate(() -> logPoolStatus(threadPoolExecutor), 1, 10, TimeUnit.SECONDS);
		
		return threadPoolExecutor;
	}
	
	/**
	 * 依赖注入日志输出
	 */
	@PostConstruct
	private void initDi() {
		log.info("############ {} Configuration DI.", this.getClass().getSimpleName());
	}
	
	/**
	 * 定时打印线程池状态
	 */
	private void logPoolStatus(ThreadPoolExecutor executor) {
		log.info("ThreadPool Status: ActiveThreads = {}, PoolSize = {}, CorePoolSize = {}, MaxPoolSize = {}, TaskCount = {}, CompletedTaskCount = {}, QueueSize = {}",
				executor.getActiveCount(),
				executor.getPoolSize(),
				executor.getCorePoolSize(),
				executor.getMaximumPoolSize(),
				executor.getTaskCount(),
				executor.getCompletedTaskCount(),
				executor.getQueue().size());
	}
}