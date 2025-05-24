package com.stephen.trajectory.utils.oss;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.transfer.Transfer;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.TransferProgress;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.bean.SpringContextHolder;
import com.stephen.trajectory.manager.oss.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ThreadUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 腾讯云COS工具类
 *
 * @author stephen qiu
 */
@Slf4j
public class CosUtils {
	
	/**
	 * 被封装的COS对象
	 */
	private static final CosManager COS_MANAGER = SpringContextHolder.getBean(CosManager.class);
	
	/**
	 * 上传文件
	 *
	 * @param file     file
	 * @param filePath filePath 文件存储路径
	 */
	public static String uploadFile(MultipartFile file, String filePath) {
		try {
			return COS_MANAGER.uploadToCos(file, filePath);
		} catch (IOException e) {
			log.error("文件上传失败: {}", e.getMessage());
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
		}
	}
	
	/**
	 * 创建 TransferManager 实例，这个实例用来后续调用高级接口
	 */
	public static TransferManager createTransferManager(COSClient cosClient) {
		// 自定义线程池大小
		// 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
		int nThreads = Runtime.getRuntime().availableProcessors();
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
		ExecutorService threadPool = new ThreadPoolExecutor(nThreads, 200,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
		// 传入一个 threadPool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
		TransferManager transferManager = new TransferManager(cosClient, threadPool);
		// 设置高级接口的配置项
		// 分块上传阈值和分块大小分别为 3MB 和 1MB
		TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
		transferManagerConfiguration.setMultipartUploadThreshold(3 * 1024 * 1024);
		transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
		transferManager.setConfiguration(transferManagerConfiguration);
		
		return transferManager;
	}
	
	/**
	 * 确定不再通过TransferManager实例进行调用高级接口后，一定要关闭掉这个实例
	 *
	 * @param transferManager 需要关闭的实例
	 */
	public static void shutdownTransferManager(TransferManager transferManager) {
		transferManager.shutdownNow(true);
	}
	
	/**
	 * 显示上传进度
	 *
	 * @param transfer 传输对象
	 */
	public static Boolean showTransferProgress(Transfer transfer) {
		// 这里的 Transfer 是异步上传结果 Upload 的父类
		log.info(transfer.getDescription());
		// transfer.isDone() 查询上传是否已经完成
		while (!transfer.isDone()) {
			try {
				// 每 5 秒获取一次进度
				ThreadUtils.sleep(Duration.ofSeconds(1));
			} catch (InterruptedException e) {
				return false;
			}
			TransferProgress progress = transfer.getProgress();
			long sofar = progress.getBytesTransferred();
			long total = progress.getTotalBytesToTransfer();
			double pct = progress.getPercentTransferred();
			log.info(String.format("Upload progress: [%d / %d] = %.02f%%\n ", sofar, total, pct));
		}
		// 完成了 Completed，或者失败了 Failed
		return Objects.equals(transfer.getState().toString(), "Completed");
	}
	
}