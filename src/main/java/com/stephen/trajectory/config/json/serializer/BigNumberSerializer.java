package com.stephen.trajectory.config.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;

/**
 * JavaScript最小值/最大值序列化器
 *
 * @author stephen qiu
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {
	
	private static final long serialVersionUID = 7296426195854670943L;
	
	/**
	 * 根据 JavaScript Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
	 */
	private static final long MAX_SAFE_INTEGER = 9007199254740991L;
	private static final long MIN_SAFE_INTEGER = -9007199254740991L;
	
	/**
	 * 提供实例
	 */
	public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);
	
	
	public BigNumberSerializer(Class<? extends Number> rawType) {
		super(rawType);
	}
	
	@Override
	public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		// 超出范围 序列化位字符串
		if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
			super.serialize(value, gen, provider);
		} else {
			gen.writeString(value.toString());
		}
	}
}
