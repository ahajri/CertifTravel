package com.ahajri.ctravel.config.jms;

import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

public class MProducer {

	@Autowired
	private JmsTemplate jmsTemplate;

	public void sendMessageToDefaultDestination(final String message) {
		try {
			jmsTemplate.convertAndSend(message);
		} catch (JmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessageToDestination(final String message, final Destination destination) {
		jmsTemplate.convertAndSend(destination, message);
	}

}
