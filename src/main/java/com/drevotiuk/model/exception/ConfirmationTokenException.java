package com.drevotiuk.model.exception;

/**
 * Custom exception class that represents errors related to confirmation tokens.
 * This exception is typically thrown when there is an issue with handling
 * confirmation tokens during operations such as user registration or email
 * verification.
 */
public class ConfirmationTokenException extends RuntimeException {
  private static final long serialVersionUID = 7485429211216895539L;

  public ConfirmationTokenException(String message) {
    super(message);
  }

  public ConfirmationTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfirmationTokenException(Throwable cause) {
    super(cause);
  }
}
