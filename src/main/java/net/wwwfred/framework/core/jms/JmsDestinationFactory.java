package net.wwwfred.framework.core.jms;

import java.util.HashMap;
import java.util.Map;

import net.wwwfred.framework.util.code.CodeUtil;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

public class JmsDestinationFactory {
    private static Map<String, ActiveMQQueue> queueMap = new HashMap<String, ActiveMQQueue>();
    private static Map<String, ActiveMQTopic> topicMap = new HashMap<String, ActiveMQTopic>();
    
    /** 获取单例的QueueDestination */
    public static ActiveMQQueue buildQueue(String name)
    {
        ActiveMQQueue result = queueMap.get(name);
        if(CodeUtil.isEmpty(result))
        {
            result = new ActiveMQQueue(name); 
        }
        return result;
    }
    
    /** 获取单例的TopicDestination */
    public static ActiveMQTopic buildTopic(String name)
    {
        ActiveMQTopic result = topicMap.get(name);
        if(CodeUtil.isEmpty(result))
        {
            result = new ActiveMQTopic(name); 
        }
        return result;
    }
}
