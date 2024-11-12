package com.stephen.trajectory.config.json;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.stephen.trajectory.config.json.serializer.BigNumberSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Spring MVC Json 配置
 *
 * @author stephen qiu
 */
@JsonComponent
public class JsonConfig {
	/**
	 * 添加 Long 转 json 精度丢失的配置
	 *
	 * @return Json自定义处理器
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer customizer() {
		return builder -> {
			// 全局配置序列化返回 JSON 处理
			JavaTimeModule javaTimeModule = new JavaTimeModule();
			javaTimeModule.addSerializer(Long.class, BigNumberSerializer.instance);
			javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.instance);
			javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.instance);
			javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
			javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
			builder.modules(javaTimeModule);
			builder.timeZone(TimeZone.getDefault());
		};
	}
}