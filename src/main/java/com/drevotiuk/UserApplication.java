package com.drevotiuk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point for the User microservice application.
 * 
 * <p>
 * This is a Spring Boot application that starts the User microservice, which is
 * responsible for authentication
 * and user management within the e-commerce system.
 * </p>
 * 
 * <p>
 * The main method uses Spring Boot's
 * {@link SpringApplication#run(Class, String...)} to launch the application.
 * </p>
 * 
 * <p>
 * Configuration and beans for this service are set up in this class and in
 * other classes annotated with
 * {@link org.springframework.stereotype.Component},
 * {@link org.springframework.context.annotation.Bean}, or
 * other Spring annotations.
 * </p>
 * 
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class UserApplication {
  /**
   * The main method that serves as the entry point for the Spring Boot
   * application.
   * 
   * <p>
   * This method initializes and starts the Spring Boot application, setting up
   * the necessary context for the
   * User microservice.
   * </p>
   * 
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(UserApplication.class, args);
  }
}
