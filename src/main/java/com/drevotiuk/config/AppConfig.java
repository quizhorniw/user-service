package com.drevotiuk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import yandex.cloud.api.kms.v1.SymmetricCryptoServiceGrpc;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceGrpc.SymmetricCryptoServiceBlockingStub;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration class for setting up application-specific beans and
 * configurations.
 * This class defines beans for password encoding, message conversion, RabbitMQ
 * integration, JSON object mapping, and external service clients.
 */
@Configuration
public class AppConfig {
  @Value("${OAUTH_TOKEN}")
  private String oauth;
  @Value("${YC_ENDPOINT}")
  private String endpoint;
  @Value("${security.password-encoder.strength}")
  private int passwordEncoderStrength;

  /**
   * Provides a {@link BCryptPasswordEncoder} bean with a strength of 10.
   * This encoder is used for hashing passwords.
   *
   * @return a {@link BCryptPasswordEncoder} instance.
   */
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(passwordEncoderStrength);
  }

  /**
   * Provides a {@link MessageConverter} bean that converts messages to and from
   * JSON format
   *
   * @return a {@link MessageConverter} instance.
   */
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * Provides an {@link AmqpTemplate} bean configured with a
   * {@link RabbitTemplate}.
   * This template is used for sending and receiving messages with RabbitMQ.
   *
   * @param connectionFactory the connection factory used to create the RabbitMQ
   *                          connection.
   * @return an {@link AmqpTemplate} instance.
   */
  @Bean
  public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter());
    return rabbitTemplate;
  }

  /**
   * Provides a {@link CredentialProvider} bean configured with OAuth token for
   * authentication in Yandex KMS.
   *
   * @return a {@link CredentialProvider} instance.
   */
  @Bean
  public CredentialProvider credentialProvider() {
    return Auth.oauthTokenBuilder()
        .oauth(oauth)
        .build();
  }

  /**
   * Provides a {@link SymmetricCryptoServiceBlockingStub} bean configured with
   * the endpoint and credential provider for accessing the Yandex KMS.
   *
   * @return a {@link SymmetricCryptoServiceBlockingStub} instance.
   */
  @Bean
  public SymmetricCryptoServiceBlockingStub symmetricCryptoService() {
    return ServiceFactory.builder()
        .endpoint(endpoint)
        .credentialProvider(credentialProvider())
        .build()
        .create(
            SymmetricCryptoServiceBlockingStub.class,
            SymmetricCryptoServiceGrpc::newBlockingStub);
  }
}
