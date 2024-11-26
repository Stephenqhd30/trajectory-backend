package com.stephen.trajectory.config.secure.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.ThrowUtils;
import com.stephen.trajectory.config.secure.enums.DeviceTypeEnum;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 设置工具类
 *
 * @author: stephen qiu
 **/
public class DeviceUtils {
	/**
	 * 获取请求设备类型
	 *
	 * @param request request
	 * @return {@link String}
	 */
	public static String getRequestDevice(HttpServletRequest request) {
		String userAgentStr = request.getHeader(Header.USER_AGENT.toString());
		ThrowUtils.throwIf(StringUtils.isBlank(userAgentStr),
				ErrorCode.OPERATION_ERROR, "User-Agent 请求头缺失");
		
		// 使用 Hutool 工具解析 User-Agent
		UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
		ThrowUtils.throwIf(userAgent == null,
				ErrorCode.OPERATION_ERROR, "无法解析 User-Agent");
		
		// 根据平台信息判断 PC 或移动端
		if (isPC(userAgent)) {
			return DeviceTypeEnum.PC.getValue();
		}
		// 判断是不是移动端
		if (userAgent.isMobile()) {
			return DeviceTypeEnum.MOBILE.getValue();
		}
		// 判断是否为小程序
		if (isMiniProgram(userAgentStr)) {
			return DeviceTypeEnum.MINI.getValue();
		}
		
		// 判断是否为平板设备
		if (isTablet(userAgentStr)) {
			return DeviceTypeEnum.TABLET.getValue();
		}
		
		
		return DeviceTypeEnum.UNKNOWN.getValue();
	}
	
	/**
	 * 判断是否为 PC
	 *
	 * @param userAgent 平台信息
	 * @return 是否为 PC
	 */
	private static boolean isPC(UserAgent userAgent) {
		String platform = userAgent.getPlatform().getName();
		return "Windows".equalsIgnoreCase(platform)
				|| "Mac".equalsIgnoreCase(platform)
				|| "Linux".equalsIgnoreCase(platform);
	}
	
	/**
	 * 判断是否为小程序请求
	 * 一般通过 User-Agent 中的 "MicroMessenger" 和 "MiniProgram" 判断
	 *
	 * @param userAgentStr User-Agent 字符串
	 * @return 是否为小程序
	 */
	public static boolean isMiniProgram(String userAgentStr) {
		return StrUtil.containsIgnoreCase(userAgentStr, "MicroMessenger")
				&& StrUtil.containsIgnoreCase(userAgentStr, "MiniProgram");
	}
	
	/**
	 * 判断是否为平板设备
	 * 支持 iOS（如 iPad）和 Android 平板的检测
	 *
	 * @param userAgentStr User-Agent 字符串
	 * @return 是否为平板设备
	 **/
	private static boolean isTablet(String userAgentStr) {
		// 检查 iPad 的 User-Agent 标志
		boolean isIpad = StrUtil.containsIgnoreCase(userAgentStr, "iPad");
		
		// 检查 Android 平板（包含 "Android" 且不包含 "Mobile"）
		boolean isAndroidTablet = StrUtil.containsIgnoreCase(userAgentStr, "Android")
				&& !StrUtil.containsIgnoreCase(userAgentStr, "Mobile");
		
		// 如果是 iPad 或 Android 平板，则返回 true
		return isIpad || isAndroidTablet;
	}
	
	
	
}

