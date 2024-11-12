package com.stephen.trajectory.constants;

/**
 * 通用常量
 *
 * @author stephen qiu
 */
public interface CommonConstant {
	
	/**
	 * 升序
	 */
	String SORT_ORDER_ASC = "ascend";
	
	/**
	 * 降序
	 */
	String SORT_ORDER_DESC = " descend";
	
	/**
	 * UTF-8 字符集
	 */
	String UTF8 = "UTF-8";
	
	/**
	 * GBK 字符集
	 */
	String GBK = "GBK";
	
	/**
	 * www主域
	 */
	String WWW = "www.";
	
	/**
	 * http请求
	 */
	String HTTP = "http://";
	
	/**
	 * https请求
	 */
	String HTTPS = "https://";
	
	/**
	 * 验证码有效期（分钟）
	 */
	long CAPTCHA_EXPIRATION = 2;
	
	/**
	 * 未知文件类型后缀
	 */
	String UNKNOWN_FILE_TYPE_SUFFIX = "unknown";
	
	/**
	 * 未知文件ContentType
	 */
	String UNKNOWN_FILE_CONTENT_TYPE = "application/octet-stream";
	
}
