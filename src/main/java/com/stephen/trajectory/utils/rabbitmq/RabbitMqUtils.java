package com.stephen.trajectory.utils.rabbitmq;

import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.config.bean.SpringContextHolder;
import com.stephen.trajectory.config.rabbitmq.defaultMq.DefaultRabbitMq;
import com.stephen.trajectory.config.rabbitmq.defaultMq.DefaultRabbitMqWithDelay;
import com.stephen.trajectory.config.rabbitmq.defaultMq.DefaultRabbitMqWithDlx;
import com.stephen.trajectory.config.rabbitmq.properties.RabbitMqProperties;
import com.stephen.trajectory.utils.rabbitmq.model.RabbitMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * RabbitMQ工具类，提供消息发送和接收功能
 *
 * @author stephen qiu
 */
@AllArgsConstructor
@Slf4j
public class RabbitMqUtils {
	
	private static final RabbitTemplate RABBITMQ_TEMPLATE =
			SpringContextHolder.getBean("rabbitTemplateBean", RabbitTemplate.class);
	
	private final RabbitMqProperties rabbitMqProperties;
	private static long maxAwaitTimeout;
	
	@PostConstruct
	private void setMaxAwaitTimeout() {
		maxAwaitTimeout = rabbitMqProperties.getMaxAwaitTimeout();
	}
	
	/**
	 * 向指定消息队列发送消息
	 *
	 * @param exchangeName 交换机名称
	 * @param routingKey   路由键
	 * @param msgId        消息ID
	 * @param message      消息体
	 */
	private static void sendMessage(String exchangeName, String routingKey, String msgId, Object message) {
		String msgText = JSONUtil.toJsonStr(message);
		RabbitMessage rabbitMessage = new RabbitMessage();
		rabbitMessage.setMsgId(msgId != null ? msgId : UUID.randomUUID().toString());
		rabbitMessage.setMsgText(msgText);
		RABBITMQ_TEMPLATE.convertAndSend(exchangeName, routingKey, JSONUtil.toJsonStr(rabbitMessage));
	}
	
	/**
	 * 向默认队列发送消息
	 *
	 * @param message 消息体
	 */
	public static void defaultSendMsg(Object message) {
		sendMessage(DefaultRabbitMq.EXCHANGE_NAME, DefaultRabbitMq.BINDING_ROUTING_KEY, null, message);
	}
	
	/**
	 * 向默认队列发送消息
	 *
	 * @param msgId   消息ID
	 * @param message 消息体
	 */
	public static void defaultSendMsg(String msgId, Object message) {
		sendMessage(DefaultRabbitMq.EXCHANGE_NAME, DefaultRabbitMq.BINDING_ROUTING_KEY, msgId, message);
	}
	
	/**
	 * 从默认队列获取消息
	 *
	 * @return 返回消息体
	 */
	public static RabbitMessage defaultReceiveMsg() {
		Object res = RABBITMQ_TEMPLATE.receiveAndConvert(DefaultRabbitMq.QUEUE_NAME, maxAwaitTimeout);
		return res != null ? JSONUtil.toBean(res.toString(), RabbitMessage.class) : null;
	}
	
	/**
	 * 向带有死信队列的队列发送消息
	 *
	 * @param message 消息体
	 */
	public static void defaultSendMsgWithDlx(Object message) {
		sendMessage(DefaultRabbitMqWithDlx.EXCHANGE_WITH_DLX_NAME,
				DefaultRabbitMqWithDlx.BINDING_WITH_DLX_ROUTING_KEY, null, message);
	}
	
	/**
	 * 从带有死信队列的队列获取消息
	 *
	 * @return 返回消息体
	 */
	public static RabbitMessage defaultReceiveMsgWithDlx() {
		Object res = RABBITMQ_TEMPLATE.receiveAndConvert(DefaultRabbitMqWithDlx.QUEUE_WITH_DLX_NAME, maxAwaitTimeout);
		return res != null ? JSONUtil.toBean(res.toString(), RabbitMessage.class) : null;
	}
	
	/**
	 * 向延迟队列发送消息
	 *
	 * @param message 消息体
	 */
	public static void defaultSendMsgWithDelay(Object message) {
		sendMessage(DefaultRabbitMqWithDelay.EXCHANGE_WITH_DELAY_NAME,
				DefaultRabbitMqWithDelay.BINDING_WITH_DELAY_ROUTING_KEY, null, message);
	}
	
	/**
	 * 从延迟队列获取消息
	 *
	 * @return 返回消息体
	 */
	public static RabbitMessage defaultReceiveMsgWithDelay() {
		Object res = RABBITMQ_TEMPLATE.receiveAndConvert(DefaultRabbitMqWithDelay.DLX_QUEUE_WITH_DELAY_NAME, maxAwaitTimeout);
		return res != null ? JSONUtil.toBean(res.toString(), RabbitMessage.class) : null;
	}
	
	/**
	 * 向自定义消息队列发送消息
	 *
	 * @param message       消息体
	 * @param rabbitmqClass 消息队列类
	 */
	public static void sendMsg(Object message, Class<?> rabbitmqClass) {
		if (isValidRabbitMqClass(rabbitmqClass)) {
			sendMessage(getExchangeName(rabbitmqClass), getBindingRoutingKey(rabbitmqClass), null, message);
		}
	}
	
	// 其他方法保持不变，依照相似方式精简...
	
	/**
	 * 校验消息队列类是否合法
	 */
	private static boolean isValidRabbitMqClass(Class<?> rabbitmqClass) {
		String className = rabbitmqClass.getName();
		if (StringUtils.equals("BaseCustomizeMq", className.split(rabbitmqClass.getPackage().getName())[1])) {
			log.error("无效的RabbitMQ类: {}", className);
			return false;
		}
		return true;
	}
	
	/**
	 * 获取交换机名称
	 */
	private static String getExchangeName(Class<?> rabbitmqClass) {
		try {
			return (String) rabbitmqClass.getField("EXCHANGE_NAME").get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("获取交换机名称失败: {}", e.getMessage());
			return null;
		}
	}
	
	/**
	 * 获取绑定路由键
	 */
	private static String getBindingRoutingKey(Class<?> rabbitmqClass) {
		try {
			return (String) rabbitmqClass.getField("BINDING_ROUTING_KEY").get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("获取绑定路由键失败: {}", e.getMessage());
			return null;
		}
	}
}
