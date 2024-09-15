package com.drevotiuk.model.exception;

/**
 * Custom exception thrown to indicate that a requested operation is forbidden.
 * This exception can be used when the user does not have the necessary
 * permissions to perform a specific action.
 */
public class ForbiddenException extends RuntimeException {
  private static final long serialVersionUID = -3420521489662093038L;

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  public ForbiddenException(Throwable cause) {
    super(cause);
  }
}
