package net.wwwfred.framework.core.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import net.wwwfred.framework.util.code.CodeUtil;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class JmsMessageSenderImpl implements JmsMessageSender {

	private JmsTemplate jmsTemplate;
	private Boolean isRedelivered;
	public void sendMessage(Boolean isDestinationQueue, final String destinationName,final Object msg)
    {
        String emptyDescription = "JMS sendMessage,param empty,isDestinationQueue="+isDestinationQueue+",destinationName="+destinationName+",jmsTemplate="+jmsTemplate;
        CodeUtil.emptyCheck(null,emptyDescription, new Object[]{isDestinationQueue,destinationName,jmsTemplate});
        
        Destination destination = isDestinationQueue?JmsDestinationFactory.buildQueue(destinationName):JmsDestinationFactory.buildTopic(destinationName);
//        System.out.println("---------------生产者发送消息目标："+isDestinationQueue+","+destinationName);   
//        System.out.println("---------------生产者发了一个消息："+msg);   
        jmsTemplate.send(destination, new MessageCreator() {  
            @Override  
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createMessage();
                message.setJMSRedelivered(isRedelivered);
                message.setObjectProperty(destinationName, msg);
                return message;
            }  
        }); 
    }
	
//	public void sendMessage(final String messageType, final String message) {
//		if(StringUtil.isEmpty(messageType)||StringUtil.isEmpty(message)){
//			return;
//		}
//		
//		MessageCreator messageCreator = new MessageCreator() {
//			public Message createMessage(Session session) throws JMSException {
//				TextMessage msg = null;
//				msg = session.createTextMessage(message);
//				msg.setStringProperty("MSG_TYPE", messageType);
//				return msg;
//			}
//		};
//		jmsTemplate.send(messageCreator);
//	}
	

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

    public Boolean getIsRedelivered() {
        return isRedelivered;
    }

    public void setIsRedelivered(Boolean isRedelivered) {
        this.isRedelivered = isRedelivered;
    }	
}
