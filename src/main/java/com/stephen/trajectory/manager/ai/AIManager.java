package com.stephen.trajectory.manager.ai;

import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * @author: stephen qiu
 * @create: 2024-06-30 16:29
 **/
@Slf4j
@Service
public class AIManager {
	
	@Resource
	private SparkClient sparkClient;
	
	private final static String CONTENT = "你是一个数据分析师和Echarts前端数据可视化专家，接下来我会按照以下固定格式给你提供内容：\n" +
			"分析需求：\n" +
			"{数据分析的需求或者目标}\n" +
			"原始数据：\n" +
			"{csv格式的原始数据，用，作为分隔符}\n" +
			"请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
			"【【【【【\n" +
			"{生成的Echarts配置代码应为JSON格式，属性名和字符串值均使用双引号，确保格式有效且代码能够正确执行。}\n" +
			"【【【【【\n" +
			"{明确的数据分析结论、越详细越好，并给出你的建议，不要生成多余的注释}";
	
	public String doChat(String message) {
		// 消息列表，可以在此列表添加历史对话记录
		List<SparkMessage> messages = new ArrayList<>();
		messages.add(SparkMessage.systemContent(CONTENT));
		messages.add(SparkMessage.userContent(message));
		// 构造请求
		SparkRequest sparkRequest = SparkRequest.builder()
				// 消息列表
				.messages(messages)
				// 模型回答的tokens的最大长度,非必传，默认为2048。
				.maxTokens(2048)
				// 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
				.temperature(1.0)
				// 指定请求版本，默认使用最新3.5版本
				.apiVersion(SparkApiVersion.V3_5)
				.build();
		String result = "";
		String useToken = "";
		try {
			// 同步调用
			SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
			SparkTextUsage textUsage = chatResponse.getTextUsage();
			result = chatResponse.getContent();
			useToken = "提问tokens：" + textUsage.getPromptTokens()
					+ "，回答tokens：" + textUsage.getCompletionTokens()
					+ "，总消耗tokens：" + textUsage.getTotalTokens();
			log.info(useToken);
		} catch (SparkException e) {
			log.error("Ai调用发生异常了：{}", e.getMessage());
			return "AI服务目前不可用，请稍后再试。";
		}
		return result;
	}
}
