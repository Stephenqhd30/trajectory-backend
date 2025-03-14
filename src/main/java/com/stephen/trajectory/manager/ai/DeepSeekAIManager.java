package com.stephen.trajectory.manager.ai;

import com.stephen.trajectory.common.ErrorCode;
import com.stephen.trajectory.common.exception.BusinessException;
import com.stephen.trajectory.config.ai.deepseek.condition.DeepSeekAICondition;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DeepSeekAIManager
 *
 * @author stephenqiu
 */
@Slf4j
@Service
@Conditional(DeepSeekAICondition.class)
public class DeepSeekAIManager {
	
	private static final String DEFAULT_MODAL = "deepseek-v3-241226";
	
	private static final String SYSTEM_CONTENT = "你是一个数据分析师和前端数据可视化开发专家。请根据以下内容进行数据分析并生成对应的图表配置。\n" +
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
	
	@Resource
	public ArkService service;
	
	
	/**
	 * 调用 AI 服务生成图表配置
	 *
	 * @param message 用户输入
	 * @return AI 服务返回的内容
	 */
	public String doChat(String message) {
		// 创建消息列表
		final List<ChatMessage> messages = new ArrayList<>();
		// 创建系统消息
		final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(SYSTEM_CONTENT).build();
		// 创建用户消息
		final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
		// 将系统消息和用户消息添加到消息列表中
		messages.add(systemMessage);
		messages.add(userMessage);
		// 创建聊天完成请求
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
				.model(DEFAULT_MODAL)
				.messages(messages)
				.build();
		
		try {
			// 调用 AI 服务生成图表配置，并使用 Collectors.joining() 将内容连接成字符串
			return service.createChatCompletion(chatCompletionRequest).getChoices().stream()
					// 提取每个生成的消息内容
					.map(choice -> choice.getMessage().getContent().toString())
					// 合并成一个字符串并返回
					.collect(Collectors.joining("\n"));
		} catch (Exception e) {
			// 错误处理：捕获异常并抛出自定义业务异常
			log.error("生成图表配置时出错：{}", e.getMessage());
			throw new BusinessException(ErrorCode.AI_ERROR, "生成图表配置时出错，请稍后重试。");
		} finally {
			// 如果是一个应用级别的单次请求，可以在应用生命周期结束时关闭服务
			service.shutdownExecutor();
		}
	}
}