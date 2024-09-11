package com.drevotiuk.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up RabbitMQ components including exchanges,
 * queues, and bindings.
 * This class defines the configuration for a direct exchange, a queue, and
 * the binding between them with a specified routing key.
 */
@Configuration
public class RabbitMQConfig {
  @Value("${rabbitmq.exchange.user-service}")
  private String userServiceExchange;
  @Value("${rabbitmq.queue.user}")
  private String userQueue;
  @Value("${rabbitmq.routingkey.user}")
  private String userRoutingKey;

  /**
   * Provides a {@link DirectExchange} bean for the user service.
   * This exchange is used for routing messages directly to the specified queue
   * based on the routing key.
   *
   * @return a {@link DirectExchange} instance.
   */
  @Bean
  public DirectExchange userServiceExchange() {
    return new DirectExchange(userServiceExchange);
  }

  /**
   * Provides a {@link Queue} bean for user-related messages.
   * This queue is used to hold messages routed from the exchange.
   *
   * @return a {@link Queue} instance.
   */
  @Bean
  public Queue userQueue() {
    return new Queue(userQueue);
  }

  /**
   * Provides a {@link Binding} bean that binds the user queue to the user service
   * exchange with a specified routing key.
   * This binding ensures that messages sent to the exchange with the routing key
   * will be routed to the user queue.
   *
   * @return a {@link Binding} instance.
   */
  @Bean
  public Binding userBinding() {
    return BindingBuilder.bind(userQueue()).to(userServiceExchange())
        .with(userRoutingKey);
  }
}
