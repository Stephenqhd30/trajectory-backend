package com.stephen.trajectory.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * 定义一个名为SingleProducer的公开类，用于实现消息发送功能
 *
 * @author stephen qiu
 */
public class SingleProducer {
	// 定义一个静态常量字符串QUEUE_NAME，它的值为"hello"，表示我们要向名为"hello"的队列发送消息
	private final static String QUEUE_NAME = "hello";
	
	// 定义程序的入口点：一个公开的静态main方法，它抛出Exception异常
	public static void main(String[] argv) throws Exception {
		// 创建一个ConnectionFactory对象，这个对象可以用于创建到RabbitMQ服务器的连接
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setPort(5672);
		// 使用ConnectionFactory创建一个新的连接,这个连接用于和RabbitMQ服务器进行交互
		try (Connection connection = factory.newConnection();
		     // 通过已建立的连接创建一个新的频道
		     Channel channel = connection.createChannel()) {
			// 在通道上声明一个队列，我们在此指定的队列名为"hello"
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			// 创建要发送的消息，这里我们将要发送的消息内容设置为"Hello World!"
			String message = "Hello World!";
			// 使用channel.basicPublish方法将消息发布到指定的队列中。这里我们指定的队列名为"hello"
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
			// 使用channel.basicPublish方法将消息发布到指定的队列中。这里我们指定的队列名为"hello"
			System.out.println(" [x] Sent '" + message + "'");
		}
	}
}
