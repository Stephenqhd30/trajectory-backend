package com.stephen.trajectory.common;


/**
 * 返回工具类
 *
 * @author stephen qiu
 */
public class ResultUtils {
	
	/**
	 * 成功，返回带数据的响应
	 *
	 * @param data 返回的数据
	 * @param <T>  数据类型
	 * @return 成功的 BaseResponse
	 */
	public static <T> BaseResponse<T> success(T data) {
		return new BaseResponse<>(0, data, "ok");
	}
	
	/**
	 * 成功，返回不带数据的响应
	 *
	 * @return 成功的 BaseResponse
	 */
	public static BaseResponse<Void> success() {
		return new BaseResponse<>(0, null, "ok");
	}
	
	/**
	 * 失败，使用 ErrorCode 枚举
	 *
	 * @param errorCode 错误码
	 * @return 失败的 BaseResponse
	 */
	public static <T> BaseResponse<T> error(ErrorCode errorCode) {
		return new BaseResponse<>(errorCode);
	}
	
	/**
	 * 失败，使用自定义错误码和消息
	 *
	 * @param code    错误码
	 * @param message 错误消息
	 * @return 失败的 BaseResponse
	 */
	public static <T> BaseResponse<T> error(int code, String message) {
		return new BaseResponse<>(code, null, message);
	}
	
	/**
	 * 失败，使用 ErrorCode 和自定义消息
	 *
	 * @param errorCode 错误码
	 * @param message   错误消息
	 * @return 失败的 BaseResponse
	 */
	public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
		return new BaseResponse<>(errorCode.getCode(), null, message);
	}
}
