package com.stephen.trajectory.config.secure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SaToken配置属性
 *
 * @author stephen qiu
 */
@Data
@ConfigurationProperties(prefix = "sa-token")
@Configuration
public class SaTokenProperties {
	
	/**
	 * 是否启用SaToken认证鉴权功能
	 */
	private Boolean enableSa = true;
	
	/**
	 * 是否使用JWT
	 */
	private Boolean enableJwt = false;
	
}