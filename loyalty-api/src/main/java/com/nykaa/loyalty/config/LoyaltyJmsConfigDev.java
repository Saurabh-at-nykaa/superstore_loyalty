package com.nykaa.loyalty.config;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

@Configuration
@EnableJms
@Profile("dev")
public class LoyaltyJmsConfigDev {

    @Value("${rabbitmq.endpoint}")
    private String rabbitMqEndPoint;

    @Value("${rabbitmq.port}")
    private String rabbitMqPort;

    @Value("${rabbitmq.username}")
    private String rabbitMqUserName;

    @Value("${rabbitmq.password}")
    private String rabbitMqPassword;

    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void initConnectionFactory() {
            RMQConnectionFactory rmqConnectionFactory = new RMQConnectionFactory();
            rmqConnectionFactory.setHost(rabbitMqEndPoint);
            rmqConnectionFactory.setUsername(rabbitMqUserName);
            rmqConnectionFactory.setPassword(rabbitMqPassword);
            rmqConnectionFactory.setPort(Integer.valueOf(rabbitMqPort));
            connectionFactory = rmqConnectionFactory;
    }

    @Bean
    @Primary
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(this.connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("1-3");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

        factory.setDestinationResolver(new DynamicDestinationResolver() {
            @Override
            public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain)
                    throws JMSException {
                RMQDestination jmsDestination = new RMQDestination();
                jmsDestination.setDestinationName(destinationName);
                jmsDestination.setAmqpQueueName(destinationName);
                jmsDestination.setAmqp(true);
                return jmsDestination;
            }
        });

        return factory;
    }


    @Bean
    public JmsTemplate defaultJmsTemplate() {
        return new JmsTemplate(this.connectionFactory);
    }

}
