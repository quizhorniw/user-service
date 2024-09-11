package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates a user was not found.
 * This exception is typically thrown when a requested user does not exist in
 * the system.
 */
public class UserNotFoundException extends RuntimeException {
  private static final long serialVersionUID = 8909120762917242496L;

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Throwable cause) {
    super(cause);
  }
}
