package net.wwwfred.framework.core.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;

public abstract class JmsMessageListenerContainer extends DefaultMessageListenerContainer implements JmsMessageHandler{
    
    private Boolean isRequestDestinationQueue;
    private String requestDestinationName;
    private Boolean isResponseDestinationQueue;
    private String responseDestinationName;
    
    public void init() {
        Destination requestDestination = isRequestDestinationQueue?JmsDestinationFactory.buildQueue(requestDestinationName):JmsDestinationFactory.buildTopic(requestDestinationName);
        setDestination(requestDestination);
        setMessageListener(new SessionAwareMessageListener<Message>() {
            @Override
            public void onMessage(Message message, Session session) throws JMSException {
//                System.out.println("onMessage invoke");
                Object msg = message.getObjectProperty(requestDestinationName);
//                    System.out.println("消费者："+requestDestinationName+",收到消息内容是：" + msg);
                try
                {
                    handleMessage(msg);
                    if(!CodeUtil.isEmpty(isResponseDestinationQueue,responseDestinationName))
                    {
                        Destination responseDestination = isResponseDestinationQueue?JmsDestinationFactory.buildQueue(responseDestinationName):JmsDestinationFactory.buildTopic(responseDestinationName);
                        MessageProducer producer = session.createProducer(responseDestination);   
                        Message responseMsg = session.createMessage();
                        responseMsg.setObjectProperty(responseDestinationName, msg);
                        producer.send(responseMsg);
//                        System.out.println("---------------生产者发送消息目标："+isResponseDestinationQueue+","+responseDestinationName);   
//                        System.out.println("---------------生产者发了一个消息：" + msg);   
                    }
                    
                    // 通知发送端已成功处理接收到的消息，不必重新发送通知消息
                    message.acknowledge();
                }
                catch(Exception e)
                {
                    LogUtil.w(getClass().getName(), "requestDestination handler msg illegal,requestDestinationName="+requestDestinationName+",msg="+msg, e);
                }
            }
        });
    }

    public Boolean getIsRequestDestinationQueue() {
        return isRequestDestinationQueue;
    }

    public void setIsRequestDestinationQueue(Boolean isRequestDestinationQueue) {
        this.isRequestDestinationQueue = isRequestDestinationQueue;
    }

    public String getRequestDestinationName() {
        return requestDestinationName;
    }

    public void setRequestDestinationName(String requestDestinationName) {
        this.requestDestinationName = requestDestinationName;
    }

    public Boolean getIsResponseDestinationQueue() {
        return isResponseDestinationQueue;
    }

    public void setIsResponseDestinationQueue(Boolean isResponseDestinationQueue) {
        this.isResponseDestinationQueue = isResponseDestinationQueue;
    }

    public String getResponseDestinationName() {
        return responseDestinationName;
    }

    public void setResponseDestinationName(String responseDestinationName) {
        this.responseDestinationName = responseDestinationName;
    }
}
