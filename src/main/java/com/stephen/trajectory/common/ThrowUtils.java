package com.stephen.trajectory.common;

import com.stephen.trajectory.common.exception.BusinessException;

/**
 * 抛异常工具类
 *
 * @author stephen qiu
 */
public class ThrowUtils {
	
	/**
	 * 条件成立则抛异常
	 *
	 * @param condition        condition
	 * @param runtimeException runtimeException
	 */
	public static void throwIf(boolean condition, RuntimeException runtimeException) {
		if (condition) {
			throw runtimeException;
		}
	}
	
	/**
	 * 条件成立则抛异常
	 *
	 * @param condition condition
	 * @param errorCode errorCode
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode) {
		throwIf(condition, new BusinessException(errorCode));
	}
	
	/**
	 * 条件成立则抛异常
	 *
	 * @param condition condition
	 * @param errorCode errorCode
	 * @param message   message
	 */
	public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
		throwIf(condition, new BusinessException(errorCode, message));
	}
}
