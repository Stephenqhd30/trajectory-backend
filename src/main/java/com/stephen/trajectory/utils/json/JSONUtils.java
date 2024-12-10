package com.stephen.trajectory.utils.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类
 *
 * @author stephen qiu
 */
@Slf4j
public class JSONUtils {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	/**
	 * 检查给定的字符串是否为合法的 JSON 格式
	 *
	 * @param jsonStr 输入的 JSON 字符串
	 * @return 如果是合法的 JSON 格式，返回 true，否则返回 false
	 */
	public static boolean isValidJson(String jsonStr) {
		if (jsonStr == null || jsonStr.trim().isEmpty()) {
			return false;
		}
		try {
			OBJECT_MAPPER.readTree(jsonStr);
			return true;
		} catch (Exception e) {
			log.warn("Invalid JSON format: {}", jsonStr, e);
			return false;
		}
	}
	
	/**
	 * 检查是否为合法的 JSON 数组
	 *
	 * @param jsonStr 输入的 JSON 字符串
	 * @return 如果是合法的 JSON 数组格式，返回 true，否则返回 false
	 */
	public static boolean isValidJsonArray(String jsonStr) {
		if (jsonStr == null || jsonStr.trim().isEmpty()) {
			return false;
		}
		try {
			return OBJECT_MAPPER.readTree(jsonStr).isArray();
		} catch (Exception e) {
			log.warn("Invalid JSON array format: {}", jsonStr, e);
			return false;
		}
	}
	
	/**
	 * 检查是否为合法的 JSON 对象
	 *
	 * @param jsonStr 输入的 JSON 字符串
	 * @return 如果是合法的 JSON 对象格式，返回 true，否则返回 false
	 */
	public static boolean isValidJsonObject(String jsonStr) {
		if (jsonStr == null || jsonStr.trim().isEmpty()) {
			return false;
		}
		try {
			return OBJECT_MAPPER.readTree(jsonStr).isObject();
		} catch (Exception e) {
			log.warn("Invalid JSON object format: {}", jsonStr, e);
			return false;
		}
	}
}
