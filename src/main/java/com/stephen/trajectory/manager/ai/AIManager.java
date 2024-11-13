package com.stephen.trajectory.manager.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.exception.BusinessException;
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
	
	private static final String CONTENT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
			"分析需求：\n" +
			"{数据分析的需求或者目标}\n" +
			"原始数据：\n" +
			"{csv格式的原始数据，用,作为分隔符}\n" +
			"请根据这两部分内容，严格按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、标题、 注释）同时不要使用这个符号 '】'\n" +
			"'【【【【【'\n" +
			"{前端 Echarts V5 的 option 配置对象 JSON 代码, 不要生成任何多余的内容，比如注释和代码块标记}\n" +
			"'【【【【【'\n" +
			"{明确的数据分析结论、越详细越好，不要生成多余的注释} \n" +
			"下面是一个具体的例子的模板：\n" +
			"'【【【【【'\n" +
			"JSON格式代码\n" +
			"'【【【【【'\n" +
			"结论：\n";
	
	/**
	 * 调用 AI 服务生成图表配置
	 *
	 * @param message 用户输入
	 * @return AI 服务返回的内容
	 */
	public String doChat(String message) {
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
				.temperature(0.2)
				// 指定请求版本，默认使用最新3.5版本
				.apiVersion(SparkApiVersion.V3_5)
				.build();
		
		try {
			SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
			SparkTextUsage textUsage = chatResponse.getTextUsage();
			log.info("AI token usage: 提问tokens：{}，回答tokens：{}，总消耗tokens：{}",
					textUsage.getPromptTokens(),
					textUsage.getCompletionTokens(),
					textUsage.getTotalTokens());
			
			String result = chatResponse.getContent();
			if (result == null || result.isEmpty()) {
				log.warn("AI response was empty.");
				return "AI服务返回了空的内容，请检查输入或稍后再试。";
			}
			
			return result.trim();
		} catch (SparkException e) {
			log.error("AI 调用发生异常: {}", e.getMessage(), e);
			return "AI服务目前不可用，请稍后再试。";
		} catch (Exception e) {
			log.error("未知错误: {}", e.getMessage(), e);
			return "服务发生未知错误，请稍后再试。";
		}
	}
}
