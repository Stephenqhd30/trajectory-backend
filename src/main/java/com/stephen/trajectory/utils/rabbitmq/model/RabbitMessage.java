package com.stephen.trajectory.utils.rabbitmq.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消息队列消息类
 *
 * @author stephen qiu
 */
@Data
@Accessors(chain = true)
public class RabbitMessage {
	
	/**
	 * 消息id
	 */
	private String msgId;
	
	/**
	 * 消息内容
	 */
	private String msgText;
	
}