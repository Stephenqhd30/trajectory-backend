package com.stephen.trajectory.manager.websocket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket 会话管理器
 *
 * @author stephen qiu
 */
@Slf4j
@Component
public class WebSocketManager {
	
	// 存储所有活跃的客户端连接
	private final ConcurrentMap<String, Channel> activeChannels = new ConcurrentHashMap<>();
	
	/**
	 * 添加新的客户端连接
	 */
	public void addChannel(Channel channel) {
		activeChannels.put(channel.id().asShortText(), channel);
		log.info("Client added: {}, Total clients: {}", channel.remoteAddress(), activeChannels.size());
	}
	
	/**
	 * 移除客户端连接
	 */
	public void removeChannel(Channel channel) {
		activeChannels.remove(channel.id().asShortText());
		log.info("Client removed: {}, Remaining clients: {}", channel.remoteAddress(), activeChannels.size());
	}
	
	/**
	 * 广播消息给所有客户端
	 */
	public void broadcast(String message) {
		activeChannels.values().forEach(channel -> {
			if (channel.isActive()) {
				channel.writeAndFlush(new TextWebSocketFrame(message));
			}
		});
		log.info("Broadcast message: {}", message);
	}
	
	/**
	 * 向指定客户端发送消息
	 */
	public boolean sendMessageToClient(String channelId, String message) {
		Channel channel = activeChannels.get(channelId);
		if (channel != null && channel.isActive()) {
			channel.writeAndFlush(new TextWebSocketFrame(message));
			log.info("Sent message to client [{}]: {}", channelId, message);
			return true;
		}
		log.warn("Client [{}] not found or inactive.", channelId);
		return false;
	}
	
	/**
	 * 获取当前活跃连接数
	 */
	public int getActiveChannelCount() {
		return activeChannels.size();
	}
}
