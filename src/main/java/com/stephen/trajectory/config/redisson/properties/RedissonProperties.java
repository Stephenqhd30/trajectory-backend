package com.stephen.trajectory.config.redisson.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: stephen qiu
 * @create: 2024-11-07 13:29
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {
	/**
	 * redis 地址
	 */
	private String host;
	
	/**
	 * redis端口
	 */
	private String port;
	
	/**
	 * redis 数据库
	 */
	private Integer database;
	
	/**
	 * redis密码
	 */
	private String password;
}
