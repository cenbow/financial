package com.mobanker.financial.mq;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

/**
 * MQ消息发送类
 * 
 * @author yinyafei
 *
 */
@Service
public class ActiveMQProducer {

	@Resource
	private JmsTemplate queueJmsTemplate;
	@Resource
	private JmsTemplate topicJmsTemplate;

	/**
	 * 发送队列map消息
	 * @param map
	 * @param activeMQQueue
	 */
	public void sendMapMsg(final Map<String, String> map, ActiveMQQueue activeMQQueue) {

		queueJmsTemplate.send(activeMQQueue, new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				for (String key : map.keySet()) {
					message.setString(key, map.get(key));
				}
				message.setStringProperty("Content-uid", "licai");
				return message;
			}
		});
	}

	/**
	 * 发送object转map队列消息
	 * @param obj
	 * @param activeMQQueue
	 */
	public void sendMapMsg(final Object obj, ActiveMQQueue activeMQQueue) {

		queueJmsTemplate.send(activeMQQueue, new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				setMessageValue(message, obj);
				message.setStringProperty("Content-uid", "licai");
				return message;
			}
		});
	}

	private void setMessageValue(MapMessage message, Object obj) {

		Field[] fields = obj.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			String fieldName = fields[i].getName();
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			Method getMethod;
			try {
				getMethod = obj.getClass().getMethod(getMethodName, new Class[] {});
				Object o = getMethod.invoke(obj, new Object[] {});
				if (o != null) {
					message.setString(fieldName, o.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送主题map消息
	 * @param map
	 * @param activeMQTopic
	 */
	public void sendMapMsg(final Map<String, String> map, ActiveMQTopic activeMQTopic) {

		topicJmsTemplate.send(activeMQTopic, new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				for (String key : map.keySet()) {
					message.setString(key, map.get(key));
				}
				message.setStringProperty("Content-uid", "licai");
				return message;
			}
		});
	}

	/**
	 * 发送主题object转map消息
	 * @param obj
	 * @param activeMQTopic
	 */
	public void sendMapMsg(final Object obj, ActiveMQTopic activeMQTopic) {

		topicJmsTemplate.send(activeMQTopic, new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage message = session.createMapMessage();
				setMessageValue(message, obj);
				message.setStringProperty("Content-uid", "licai");
				return message;
			}
		});
	}
}
