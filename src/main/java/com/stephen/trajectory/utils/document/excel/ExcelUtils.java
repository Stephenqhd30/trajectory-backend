package com.stephen.trajectory.utils.document.excel;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.exception.BusinessException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Excel 工具类，提供 Excel 文件的创建、响应设置等功能
 *
 * @author stephen qiu
 */
public class ExcelUtils {
	
	/**
	 * 获取当前类的资源路径
	 *
	 * @return 当前路径
	 */
	public static String getPath() {
		return Objects.requireNonNull(ExcelUtils.class.getResource("/")).getPath();
	}
	
	/**
	 * 创建新的文件，若文件已存在则删除旧文件
	 *
	 * @param pathName 文件名
	 * @return 新创建的文件
	 */
	public static File createNewFile(String pathName) {
		File file = new File(getPath() + pathName);
		
		// 如果文件已存在，尝试删除旧文件并检查是否成功
		if (file.exists()) {
			boolean deleted = file.delete();
			if (!deleted) {
				throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法删除文件: " + file.getPath());
			}
		} else {
			// 创建父目录（如果不存在），并检查是否成功
			if (!file.getParentFile().exists()) {
				boolean dirsCreated = file.getParentFile().mkdirs();
				if (!dirsCreated) {
					throw new BusinessException(ErrorCode.OPERATION_ERROR, "无法创建目录: " + file.getParentFile().getPath());
				}
			}
		}
		return file;
	}
	
	/**
	 * 设置 HTTP 响应属性，以便下载 Excel 文件
	 *
	 * @param response    HTTP 响应对象
	 * @param rawFileName 导出的文件名
	 */
	public static void setExcelResponseProp(HttpServletResponse response, String rawFileName) {
		// 设置内容类型和字符编码
		response.setContentType("application/vnd.vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		
		// 对文件名进行编码，避免中文乱码
		String fileName = URLEncoder.encode(rawFileName.concat(".xlsx"), StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
	}
	
	/**
	 * 将 Date 对象转换为字符串格式
	 *
	 * @param date 日期对象
	 * @return 转换后的日期字符串
	 * @throws BusinessException 如果日期为 null
	 */
	public static String dateToString(Date date) {
		if (date == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "日期不可为 null");
		}
		// 使用指定格式转换日期为字符串
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
}
