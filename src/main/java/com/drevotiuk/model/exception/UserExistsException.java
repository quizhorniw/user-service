package com.drevotiuk.model.exception;

/**
 * Custom exception class that indicates an attempt to register a user with
 * the same credentials (e.g., username or email) already exists in the system.
 * This exception is typically thrown during user registration when a user with
 * the same identifier is found in the database.
 */
public class UserExistsException extends RuntimeException {
  private static final long serialVersionUID = 6008056172163723728L;

  public UserExistsException(String message) {
    super(message);
  }

  public UserExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserExistsException(Throwable cause) {
    super(cause);
  }
}
