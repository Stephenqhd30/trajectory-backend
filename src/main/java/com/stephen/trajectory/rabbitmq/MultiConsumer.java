package com.stephen.trajectory.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * 多消费者
 *
 * @author stephen qiu
 */
public class MultiConsumer {
	// 声明队列名称为"multi_queue"
	private static final String TASK_QUEUE_NAME = "multi_queue";
	
	public static void main(String[] argv) throws Exception {
		// 创建一个新的连接工厂
		ConnectionFactory factory = new ConnectionFactory();
		// 设置连接工厂的主机地址
		factory.setHost("localhost");
		// 从工厂获取一个新的连接
		final Connection connection = factory.newConnection();
		// 从连接获取一个新的通道
		final Channel channel = connection.createChannel();
		// 声明一个队列,并设置属性:队列名称,持久化,非排他,非自动删除,其他参数;如果队列不存在,则创建它
		channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
		// 在控制台打印等待消息的提示信息
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		// (这个先注释)设置预取计数为1，这样RabbitMQ就会在给消费者新消息之前等待先前的消息被确认
		// channel.basicQos(1);
		// 创建消息接收回调函数,以便接收消息
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			// 将接收到的消息转为字符串
			String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
			try {
				// (放到try里)打印出接收到的消息
				System.out.println(" [x] Received '" + message + "'");
				// 处理工作,模拟处理消息所花费的时间,机器处理能力有限(接收一条消息,20秒后再接收下一条消息)
				Thread.sleep(5000);
				// (不用doWork)模拟消息处理工作
				// doWork(message);
			} catch (InterruptedException e) {
				// 模拟处理消息所花费的时间
				throw new RuntimeException(e);
			} finally {
				// 打印出完成消息处理的提示
				System.out.println(" [x] Done");
				// 手动发送应答,告诉RabbitMQ消息已经被处理
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		};
		// 开始消费消息,传入队列名称,是否自动确认,投递回调和消费者取消回调
		channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
		});
	}
}
