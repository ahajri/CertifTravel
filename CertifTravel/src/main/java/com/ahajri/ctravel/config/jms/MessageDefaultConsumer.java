package com.ahajri.ctravel.config.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

public class MessageDefaultConsumer implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(MessageDefaultConsumer.class);

	@Override
	public void onMessage(Message message) {
		LOGGER.debug("Received Message: " + message);
		if (message instanceof TextMessage) {
			try {
				String msg = ((TextMessage) message).getText();
				System.out.println("###############Message has been consumed : " + msg);
			} catch (JMSException ex) {
				throw new RuntimeException(ex);
			}
		} else {
			throw new IllegalArgumentException("Message must be of type TextMessage");
		}

	}

}
