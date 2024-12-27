package com.stephen.trajectory.config.websocket.handler;

import com.stephen.trajectory.manager.websocket.WebSocketManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 自定义WebSocket数据处理Handler
 * 功能概述：
 * - 处理客户端通过WebSocket发送的消息；
 * - 维护客户端连接（新增和移除）；
 * - 处理异常和错误，确保连接安全性；
 * - 利用WebSocketManager实现消息的广播和管理。
 *
 * @author stephen
 */
@Slf4j
@Component
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	
	@Resource
	private WebSocketManager webSocketManager;
	
	/**
	 * 处理收到的TextWebSocketFrame消息
	 *
	 * @param ctx ChannelHandler上下文，用于获取连接信息
	 * @param msg 客户端发送的消息，以TextWebSocketFrame形式
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
		// 获取消息内容
		String message = msg.text();
		log.info("Received message: {}", message);
		
		// 调用WebSocketManager广播消息
		webSocketManager.broadcast(message);
	}
	
	/**
	 * 新的客户端连接建立时触发
	 *
	 * @param ctx ChannelHandler上下文，用于标识连接
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		// 将新连接的Channel添加到WebSocketManager中进行管理
		webSocketManager.addChannel(ctx.channel());
		log.info("New connection added: {}", ctx.channel().id());
	}
	
	/**
	 * 客户端连接关闭时触发
	 *
	 * @param ctx ChannelHandler上下文，用于标识关闭的连接
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		// 将关闭的Channel从WebSocketManager中移除
		webSocketManager.removeChannel(ctx.channel());
		log.info("Connection removed: {}", ctx.channel().id());
	}
	
	/**
	 * 异常处理逻辑
	 *
	 * @param ctx   ChannelHandler上下文
	 * @param cause 异常对象，描述发生的错误
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 记录异常信息
		log.error("Error occurred: ", cause);
		
		// 关闭异常连接以释放资源
		ctx.close();
	}
}
