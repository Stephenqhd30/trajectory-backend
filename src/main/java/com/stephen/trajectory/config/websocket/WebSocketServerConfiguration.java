package com.stephen.trajectory.config.websocket;

import com.stephen.trajectory.config.websocket.condition.WebSocketCondition;
import com.stephen.trajectory.config.websocket.handler.TextWebSocketFrameHandler;
import com.stephen.trajectory.config.websocket.properties.WebSocketProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * WebSocket服务器配置类
 * <p>
 * 功能概述：
 * - 初始化并启动WebSocket服务器；
 * - 管理Netty的线程组资源；
 * - 设置WebSocket协议支持和消息处理；
 * - 在Spring容器销毁时释放资源。
 * </p>
 *
 * @author stephen qiu
 */
@Configuration
@Slf4j
@Conditional(WebSocketCondition.class)
public class WebSocketServerConfiguration {
	
	// 用于处理连接请求的线程组
	private EventLoopGroup connectionAcceptorGroup;
	
	// 用于处理IO和业务逻辑的线程组
	private EventLoopGroup ioWorkerGroup;
	
	@Resource
	private WebSocketProperties webSocketProperties;
	
	@Resource
	private TextWebSocketFrameHandler textWebSocketFrameHandler;
	
	/**
	 * 初始化并启动WebSocket服务器
	 * 使用@PostConstruct注解保证在Spring容器加载时自动调用。
	 */
	@PostConstruct
	public void initializeWebSocketServer() {
		// 检查端口号是否合法
		if (webSocketProperties.getPort() > 65535 || webSocketProperties.getPort() < 0) {
			log.warn("Invalid WebSocket port [{}] configured. Using default port 39999.", webSocketProperties.getPort());
			webSocketProperties.setPort(39999);
		}
		
		// 初始化Netty线程组
		connectionAcceptorGroup = new NioEventLoopGroup(webSocketProperties.getBossThread());
		ioWorkerGroup = new NioEventLoopGroup(webSocketProperties.getWorkerThread());
		
		try {
			// 配置ServerBootstrap用于启动Netty服务器
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(connectionAcceptorGroup, ioWorkerGroup)
					// 设置服务端通道类型
					.channel(NioServerSocketChannel.class)
					// 配置连接队列大小
					.option(ChannelOption.SO_BACKLOG, 128)
					// 保持连接活跃
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					// 日志处理器
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<NioSocketChannel>() {
						@Override
						protected void initChannel(NioSocketChannel nioSocketChannel) {
							// 配置Pipeline处理请求
							ChannelPipeline pipeline = nioSocketChannel.pipeline();
							// HTTP编解码器
							pipeline.addLast(new HttpServerCodec());
							// 支持大数据流
							pipeline.addLast(new ChunkedWriteHandler());
							// 聚合HTTP请求
							pipeline.addLast(new HttpObjectAggregator(8192));
							// WebSocket协议支持
							pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
							// 自定义Handler
							pipeline.addLast(textWebSocketFrameHandler);
						}
					});
			
			// 异步绑定服务器端口并启动
			ChannelFuture bindFuture = serverBootstrap.bind(webSocketProperties.getPort()).sync();
			log.info("WebSocket Server is RUNNING on port {}.", webSocketProperties.getPort());
			
			// 监听关闭事件
			bindFuture.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> {
				if (channelFuture.isSuccess()) {
					log.info("WebSocket Server is SHUTTING DOWN.");
				} else {
					log.error("WebSocket Server shutdown error: ", channelFuture.cause());
				}
			});
		} catch (InterruptedException e) {
			log.error("WebSocket Server interrupted: ", e);
			// 恢复中断状态
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * 停止WebSocket服务器并释放资源
	 * 使用@PreDestroy注解保证在Spring容器销毁时自动调用。
	 */
	@PreDestroy
	public void shutdownWebSocketServer() {
		// 优雅关闭线程组，释放资源
		if (connectionAcceptorGroup != null) {
			connectionAcceptorGroup.shutdownGracefully();
		}
		if (ioWorkerGroup != null) {
			ioWorkerGroup.shutdownGracefully();
		}
		log.info("WebSocket Server resources released.");
	}
}
