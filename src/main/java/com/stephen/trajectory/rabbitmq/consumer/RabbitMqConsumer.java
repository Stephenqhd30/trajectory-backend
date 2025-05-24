package com.stephen.trajectory.rabbitmq.consumer;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.stephen.trajectory.rabbitmq.consumer.model.RabbitMessage;
import com.stephen.trajectory.rabbitmq.defaultMq.DefaultRabbitMq;
import com.stephen.trajectory.rabbitmq.defaultMq.DefaultRabbitMqWithDelay;
import com.stephen.trajectory.rabbitmq.defaultMq.DefaultRabbitMqWithDlx;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;

/**
 * 消息队列 RabbitMQ 消费端编码示例
 *
 * @author stephen qiu
 */
public class RabbitMqConsumer {
	
	/**
	 * 处理普通队列消息
	 *
	 * @param message 消息内容
	 * @param channel RabbitMQ 通道
	 * @param tag     当前消息的 Delivery Tag
	 * @throws IOException 抛出异常
	 */
	@RabbitHandler
	@RabbitListener(queues = DefaultRabbitMq.QUEUE_NAME)
	public void messageConsumer1(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		processMessage(message, channel, tag, "普通队列");
	}
	
	/**
	 * 处理带有死信的队列消息
	 *
	 * @param message 消息内容
	 * @param channel RabbitMQ 通道
	 * @param tag     当前消息的 Delivery Tag
	 * @throws IOException 抛出异常
	 */
	@RabbitHandler
	@RabbitListener(queues = DefaultRabbitMqWithDlx.QUEUE_WITH_DLX_NAME)
	public void messageConsumer2(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		processMessage(message, channel, tag, "死信队列");
	}
	
	/**
	 * 处理死信队列中的消息
	 *
	 * @param message 消息内容
	 * @param channel RabbitMQ 通道
	 * @param tag     当前消息的 Delivery Tag
	 * @throws IOException 抛出异常
	 */
	@RabbitHandler
	@RabbitListener(queues = DefaultRabbitMqWithDlx.DLX_QUEUE_WITH_DLX_NAME)
	public void messageConsumer3(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		processMessage(message, channel, tag, "死信队列消息");
	}
	
	/**
	 * 处理延时队列消息
	 *
	 * @param message 消息内容
	 * @param channel RabbitMQ 通道
	 * @param tag     当前消息的 Delivery Tag
	 * @throws IOException 抛出异常
	 */
	@RabbitHandler
	@RabbitListener(queues = DefaultRabbitMqWithDelay.DLX_QUEUE_WITH_DELAY_NAME)
	public void messageConsumer4(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
		processMessage(message, channel, tag, "延时队列");
	}
	
	/**
	 * 公用的消息处理逻辑
	 *
	 * @param message   消息内容
	 * @param channel   RabbitMQ 通道
	 * @param tag       当前消息的 Delivery Tag
	 * @param queueType 队列类型描述（如 "普通队列"、"死信队列" 等）
	 * @throws IOException 抛出异常
	 */
	private void processMessage(Message message, Channel channel, long tag, String queueType) throws IOException {
		try {
			// 使用 Hutool 的 JSON 工具将消息体转换为 RabbitMessage 对象
			RabbitMessage rabbitMessage = JSONUtil.toBean(new String(message.getBody()), RabbitMessage.class);
			System.out.printf("[%s] 收到消息: %s%n", queueType, rabbitMessage);
			// 手动确认消息已被成功处理
			channel.basicAck(tag, false);
		} catch (Exception e) {
			System.err.printf("[%s] 消息处理失败: %s%n", queueType, e.getMessage());
			// 出现异常时，拒绝并丢弃消息（不重发）
			channel.basicNack(tag, false, false);
		}
	}
}
