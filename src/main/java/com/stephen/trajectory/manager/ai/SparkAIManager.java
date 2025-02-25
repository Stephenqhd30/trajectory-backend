package com.stephen.trajectory.manager.ai;

import com.stephen.trajectory.config.ai.spark.condition.SparkAICondition;
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
public class SparkAIManager {
	
	@Resource
	private SparkClient sparkClient;
	
	
	private static final String CONTENT = "你是一个数据分析师和前端数据可视化开发专家。请根据以下内容进行数据分析并生成对应的图表配置。\n" +
			"\n" +
			"输入格式：\n" +
			"1. 分析需求：简要描述需要的数据分析目标。\n" +
			"2. 原始数据：CSV 格式的数据，使用逗号 (,) 分隔。\n" +
			"3. 任务要求：\n" +
			"   - 请生成一个 ECharts V5 图表配置（option 配置对象）。\n" +
			"   - 返回的配置必须是标准 JSON 格式。\n" +
			"   - 图表的坐标轴、类型和数据点应与原始数据匹配。\n" +
			"   - 提供合理的默认值，如坐标轴范围、颜色配置等。\n" +
			"   - 不要生成任何额外的注释或格式化说明。\n" +
			"   - 生成的配置必须通过 JSON 解析器验证。\n" +
			"\n" +
			"输出格式：\n" +
			"1. 第一部分：图表配置：返回 ECharts V5 的完整 JSON 配置，直接返回配置对象，不需要额外的标记或解释。\n" +
			"2. 第二部分：数据分析结论：基于原始数据生成详细的分析结论。结论应当简洁且具体，避免模糊描述。\n" +
			"\n" +
			"请确保：\n" +
			"- 第一部分和第二部分之间使用 `'【【【【【'` 分隔符。\n" +
			"- 确保返回的 JSON 配置是有效的，不包含额外的格式化标记。\n" +
			"示例：\n" +
			"- 分析需求：展示不同时间段内的销售额和增长趋势。\n" +
			"- 原始数据：Date, Sales\\n2024-01-01, 100\\n2024-01-02, 120\\n2024-01-03, 150\\n2024-01-04, 130\\n2024-01-05, 160\n" +
			"- 任务要求：\n" +
			"   - 绘制一个折线图，展示销售额随时间的变化。\n" +
			"   - 坐标轴应根据日期和销售额自动生成合理范围。\n" +
			"\n" +
			"请严格按照上述格式生成内容。不要输出任何其他多余的信息。";
	
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
