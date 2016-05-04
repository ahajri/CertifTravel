package com.ahajri.ctravel.config;

import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.ahajri.ctravel.config.jms.MProducer;
import com.ahajri.ctravel.config.jms.MessageDefaultConsumer;

/**
 * 
 * @author:
 *         <p>
 *         ahajri
 *         </p>
 */
@Configuration
@ComponentScan
@PropertySource({ "classpath:jms.properties", "classpath:jndi.properties" })
public class JmsConfig {

	private static final Logger LOGGER = Logger.getLogger(JmsConfig.class);

//	@Value("${java.naming.provider.url}")
	private String brokerURL="failover:tcp://localhost:61616";

	@Bean
	public ActiveMQConnectionFactory jmsConnectionFactory() {
		LOGGER.debug("Broker URL: " + brokerURL);
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
		return factory;
	}

	@Bean
	public static PropertyPlaceholderConfigurer jmsProperties() {
		PropertyPlaceholderConfigurer jmsProperties = new PropertyPlaceholderConfigurer();
		jmsProperties.setOrder(99999);
		jmsProperties.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");
		jmsProperties.setIgnoreUnresolvablePlaceholders(true);
		Properties properties = new Properties();
		properties.setProperty("JMS.BROKER.URL", "tcp://localhost:61616");
		properties.setProperty("JMS.QUEUE.NAME", "BTALK_QUEUE");
		jmsProperties.setProperties(properties);
		return jmsProperties;
	}

	@Bean
	public CachingConnectionFactory connectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setTargetConnectionFactory(jmsConnectionFactory());
		return factory;
	}

	@Bean
	public ActiveMQQueue destinationQueue() {
		ActiveMQQueue queue = new ActiveMQQueue("BTALK_QUEUE");
		return queue;
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate(jmsConnectionFactory());
		jmsTemplate.setDefaultDestination(destinationQueue());
		return jmsTemplate;
	}

	@Bean
	public MProducer messageProducer() {
		return new MProducer();
	}

	@Bean
	public MessageDefaultConsumer messageDefaultConsumer() {
		return new MessageDefaultConsumer();
	}

	@Bean
	public DefaultMessageListenerContainer defaultMessageListenerContainer() {
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory());
		container.setDestination(destinationQueue());
		container.setMessageListener(messageDefaultConsumer());
		return container;
	}

}
