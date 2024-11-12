package com.stephen.trajectory.utils.document.excel;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel 工具类，提供 Excel 文件的创建、响应设置等功能
 *
 * @author stephen qiu
 */
@Slf4j
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
	
	/**
	 * 将 excel 文件转换为 csv
	 *
	 * @param multipartFile multipartFile
	 * @return {@link String}
	 */
	public static String excelToCsv(MultipartFile multipartFile) {
		List<Map<Integer, String>> list = null;
		try {
			list = EasyExcel.read(multipartFile.getInputStream())
					.excelType(ExcelTypeEnum.XLSX)
					.sheet()
					.headRowNumber(0)
					.doReadSync();
		} catch (IOException e) {
			log.error("表格处理错误 :{}", e.getMessage());
			throw new RuntimeException(e);
		}
		
		if (CollUtil.isEmpty(list)) {
			return "";
		}
		// 转换为 csv
		StringBuilder stringBuilder = new StringBuilder();
		// 读取表头
		LinkedHashMap<Integer, String> headerMap = (LinkedHashMap) list.get(0);
		List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
		stringBuilder.append(StringUtils.join(headerList, ",")).append("\n");
		// 读取数据
		for (int i = 1; i < list.size(); i++) {
			LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
			List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
			stringBuilder.append(StringUtils.join(dataList, ",")).append("\n");
		}
		return stringBuilder.toString();
	}
}
