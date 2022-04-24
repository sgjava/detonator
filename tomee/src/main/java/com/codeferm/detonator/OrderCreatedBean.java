/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.codeferm.dto.Orders;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;

/**
 * MDB used for asynchronous order creation using multiple sessions.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "10")})
public class OrderCreatedBean implements MessageListener {

    /**
     * Logger.
     */
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(OrderCreatedBean.class);
    /**
     * Business object.
     */
    @Inject
    private OrdersBo ordersBo;
    /**
     * Create order logic.
     */
    private OrderShipped orderShipped;

    /**
     * Load properties file from class path.
     *
     * @param propertyFile Name of property file.
     * @return Properties.
     */
    public Properties loadProperties(final String propertyFile) {
        Properties props = new Properties();
        // Get properties from classpath
        try (final var stream = DaoProducer.class.getClassLoader().getResourceAsStream(propertyFile)) {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Property file exception", e);
        }
        return props;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.debug("PostConstruct");
        Properties properties = loadProperties("app.properties");
        orderShipped = new OrderShipped(properties.getProperty("template.dir"), properties.getProperty(
                "template"), properties.getProperty("output.dir"), ordersBo, Integer.parseInt(properties.getProperty(
                "order.shipped.max.threads")));
    }

    /**
     * Destroy cache.
     */
    @PreDestroy
    public void destroy() {
        logger.debug("PreDestroy");
    }

    /**
     * A MessageListener is used to receive asynchronously delivered messages. Each session must insure that it passes messages
     * serially to the listener. This means that a listener assigned to one or more consumers of the same session can assume that
     * the onMessage method is not called with the next message until the session has completed the last call.
     *
     * @param message Message.
     */
    @Override
    public void onMessage(final Message message) {
        final var objectMessage = (ObjectMessage) message;
        try {
            orderShipped.shipOrder((Orders) objectMessage.getObject());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
