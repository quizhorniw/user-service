package com.drevotiuk.model.exception;

/**
 * Custom exception class that represents an error when an encrypted secret key
 * is not found in the database.
 * This exception is typically thrown when a requested secret key cannot be
 * located or retrieved from the storage.
 */
public class KeyNotFoundException extends Exception {
  private static final long serialVersionUID = 2432152956574227829L;

  public KeyNotFoundException(String message) {
    super(message);
  }

  public KeyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyNotFoundException(Throwable cause) {
    super(cause);
  }
}
