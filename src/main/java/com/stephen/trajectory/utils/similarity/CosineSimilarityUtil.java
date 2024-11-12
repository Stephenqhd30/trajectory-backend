package com.stephen.trajectory.utils.similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算余弦相似度工具类
 *
 * @author stephen qiu
 */
public class CosineSimilarityUtil {
	
	/**
	 * 计算余弦相似度
	 *
	 * @param tag1 第一个标签列表
	 * @param tag2 第二个标签列表
	 * @return 余弦相似度值
	 */
	public static double cosineSimilarity(List<String> tag1, List<String> tag2) {
		// 将两个标签列表转换为向量
		Map<String, Integer> vector1 = getVector(tag1);
		Map<String, Integer> vector2 = getVector(tag2);
		
		// 计算两个向量的点积
		int dotProduct = 0;
		for (String key : vector1.keySet()) {
			if (vector2.containsKey(key)) {
				dotProduct += vector1.get(key) * vector2.get(key);
			}
		}
		
		// 计算两个向量的模长
		double magnitude1 = getMagnitude(vector1);
		double magnitude2 = getMagnitude(vector2);
		
		// 避免除以零的情况
		if (magnitude1 == 0 || magnitude2 == 0) {
			return 0.0;
		}
		
		// 计算余弦相似度
		return dotProduct / (magnitude1 * magnitude2);
	}
	
	/**
	 * 将标签列表转换为向量
	 *
	 * @param tags 标签列表
	 * @return 标签向量
	 */
	private static Map<String, Integer> getVector(List<String> tags) {
		Map<String, Integer> vector = new HashMap<>();
		for (String tag : tags) {
			vector.put(tag, vector.getOrDefault(tag, 0) + 1);
		}
		return vector;
	}
	
	/**
	 * 计算向量的模长
	 *
	 * @param vector 向量
	 * @return 向量的模长
	 */
	private static double getMagnitude(Map<String, Integer> vector) {
		double magnitude = 0;
		for (int value : vector.values()) {
			magnitude += Math.pow(value, 2);
		}
		return Math.sqrt(magnitude);
	}
}