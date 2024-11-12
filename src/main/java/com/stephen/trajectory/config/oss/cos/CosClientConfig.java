package com.stephen.trajectory.config.oss.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.stephen.trajectory.config.oss.cos.condition.CosCondition;
import com.stephen.trajectory.config.oss.cos.properties.CosProperties;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 腾讯云对象存储客户端
 *
 * @author stephen qiu
 */
@Configuration
@Data
@Conditional(CosCondition.class)
public class CosClientConfig {
	
	@Resource
	private CosProperties cosProperties;
	
	@Bean
	public COSClient cosClient() {
		// 初始化用户身份信息(secretId, secretKey)
		COSCredentials cred = new BasicCOSCredentials(cosProperties.getAccessKey(), cosProperties.getSecretKey());
		// 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
		ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
		// 生成cos客户端
		return new COSClient(cred, clientConfig);
	}
}