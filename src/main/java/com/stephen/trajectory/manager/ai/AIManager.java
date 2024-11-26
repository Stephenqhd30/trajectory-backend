package com.stephen.trajectory.manager.ai;

import com.stephen.trajectory.config.ai.condition.SparkAICondition;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
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
@Conditional(SparkAICondition.class)
public class AIManager {
	
	@Resource
	private SparkClient sparkClient;
	
	
	private static final String CONTENT = "你是一个数据分析师和前端数据可视化开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
			"分析需求：\n" +
			"{数据分析的需求目标}\n" +
			"原始数据：\n" +
			"{csv格式的原始数据，用,作为分隔符}\n" +
			"任务要求：\n" +
			"1. 严格按照以下输出格式生成：\n" +
			"'【【【【【'\n" +
			"  第 1 部分：返回前端 ECharts V5 的 option 配置对象，格式为标准 JSON，不要生成多余的注释、标记或格式化说明。\n" +
			"'【【【【【'\n" +
			"  第 2 部分：返回基于数据的详细分析结论。\n" +
			"2. 确保输出 JSON 数据中：\n" +
			"   坐标轴、图表类型等与原始数据匹配。\n" +
			"   数据点不丢失或重复。\n" +
			"   提供合理的默认值（如坐标轴范围、颜色配置）。\n" +
			"3. 遵循以下约束：\n" +
			"   确保图表的逻辑正确性（如坐标轴和数据的维度一致）。\n" +
			"   确保图表的结构完整性。\n" +
			"   输出必须在 JSON 解析器中通过验证。\n" +
			"   提供结论时，避免模糊用词，尽可能具体。" +
			"请根据我的要求，严格按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、 注释）同时不要使用这个符号 '】'\n" +
			"下面是一个具体的例子的模板：\n" +
			"'【【【【【'\n" +
			"{前端 Echarts V5 的 option 配置对象 JSON 代码, 不要生成任何多余的内容，比如注释和代码块标记}\n" +
			"'【【【【【'\n" +
			"{明确的数据分析结论、越详细越好，不要生成多余的注释} \n";
	
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
				.maxTokens(4096)
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
