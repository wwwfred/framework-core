package net.wwwfred.framework.core.jms;

public interface JmsMessageSender {

	/**
	 * 发送MQ消息
	 * @param isDestinationQueue 是否为点对点模式的消息，否则为发布/订阅模式的消息
	 * @param destinationName 消息目的地名称
	 * @param msg 消息对象
	 */
    void sendMessage(Boolean isDestinationQueue, String destinationName, Object msg);
}
