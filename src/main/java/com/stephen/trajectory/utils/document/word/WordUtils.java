package com.stephen.trajectory.utils.document.word;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;

/**
 * @description: word工具类
 * @author: stephen qiu
 * @create: 2024-11-09 16:07
 **/
@Slf4j
public class WordUtils {
	
	/**
	 * 将文件流转换为 MultipartFile
	 *
	 * @param fileInputStream 文件输入流
	 * @param filePath        文件路径
	 * @return {@link MultipartFile}
	 * @throws IOException 异常
	 */
	public static MultipartFile convertToMultipartFile(FileInputStream fileInputStream, String filePath) throws IOException {
		File generatedFile = new File(filePath);
		FileItem fileItem = new DiskFileItem("file", "application/octet-stream", false, generatedFile.getName(), (int) generatedFile.length(), generatedFile.getParentFile());
		try (OutputStream outputStream = fileItem.getOutputStream()) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}
		return new CommonsMultipartFile(fileItem);
	}
	
	
	/**
	 * 将Word文档转换为PDF
	 *
	 * @param inputFilePath  输入Word文件路径
	 * @param outputFilePath 输出PDF文件路径
	 */
	public static void convertToPdf(String inputFilePath, String outputFilePath) {
		// 获取当前操作系统类型
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			// Windows 环境：使用 documents4j 或 LibreOffice 的命令行工具
			windowsConvertToPdf(inputFilePath, outputFilePath);
		} else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
			// Linux 环境：使用 LibreOffice 的命令行工具
			linuxConvertToPdf(inputFilePath, outputFilePath);
		} else {
			throw new BusinessException(ErrorCode.WORD_ERROR, "当前操作系统不支持此操作");
		}
	}
	
	/**
	 * windows 转换为 pdf
	 *
	 * @param inputFilePath  inputFilePath
	 * @param outputFilePath outputFilePath
	 */
	private static void windowsConvertToPdf(String inputFilePath, String outputFilePath) {
		// 使用 documents4j 进行转换
		File inputWord = new File(inputFilePath);
		File outputFile = new File(outputFilePath);
		try (InputStream docxInputStream = new FileInputStream(inputWord);
		     OutputStream outputStream = new FileOutputStream(outputFile)) {
			
			IConverter converter = LocalConverter.builder().build();
			converter.convert(docxInputStream)
					.as(DocumentType.DOCX)
					.to(outputStream)
					.as(DocumentType.PDF)
					.execute();
			
			log.info("Word 文件已成功转换为 PDF 文件 (Windows)");
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.WORD_ERROR, "证书生成失败 (Windows): " + e.getMessage());
		}
	}
	
	/**
	 * linux 转换为 pdf
	 *
	 * @param inputFilePath  inputFilePath
	 * @param outputFilePath outputFilePath
	 */
	private static void linuxConvertToPdf(String inputFilePath, String outputFilePath) {
		// 构建 LibreOffice 的命令行转换命令
		String command = String.format(
				"libreoffice --headless --invisible --convert-to pdf %s --outdir %s",
				inputFilePath,
				new File(outputFilePath).getParent()
		);
		
		// 执行转换命令
		try {
			executeLinuxCmd(command);
			log.info("Word 文件已成功转换为 PDF 文件 (Linux)");
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.WORD_ERROR, "证书生成失败 (Linux): " + e.getMessage());
		}
	}
	
	/**
	 * 执行linux下的命令
	 *
	 * @param cmd cmd
	 */
	private static void executeLinuxCmd(String cmd) throws IOException {
		Process process = Runtime.getRuntime().exec(cmd);
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			log.error("executeLinuxCmd 执行Linux命令异常：", e);
			Thread.currentThread().interrupt();
		}
	}
	
}

