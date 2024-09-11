package com.drevotiuk.model.exception;

/**
 * Custom exception class that represents errors related to the use of an
 * invalid or unsupported algorithm.
 * This exception is typically thrown when an algorithm is specified that is not
 * recognized or supported by the system.
 */
public class InvalidAlgorithmException extends RuntimeException {
  private static final long serialVersionUID = 7119204930247445773L;

  public InvalidAlgorithmException(String message) {
    super(message);
  }

  public InvalidAlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidAlgorithmException(Throwable cause) {
    super(cause);
  }
}
