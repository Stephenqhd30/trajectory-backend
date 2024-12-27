package com.stephen.trajectory.config.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket配置属性
 *
 * @author AntonyCheng
 */
@Data
@ConfigurationProperties(prefix = "websocket")
@Configuration
public class WebSocketProperties {
	
	/**
	 * 是否启用WebSocket服务
	 */
	private Boolean enable = false;
	
	/**
	 * WebSocket端口，范围只允许为0-65535中的整数
	 */
	private Integer port = 39999;
	
	/**
	 * BOSS线程组线程数
	 */
	private Integer bossThread = 4;
	
	/**
	 * WORKER线程组线程数
	 */
	private Integer workerThread = 16;
	
}
