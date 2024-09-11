package com.drevotiuk;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.jsonwebtoken.JwtException;

import com.drevotiuk.model.exception.ConfirmationTokenException;
import com.drevotiuk.model.exception.UserExistsException;
import com.drevotiuk.model.exception.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler that intercepts and handles exceptions globally in
 * the application.
 * Provides specific handling for various exceptions and returns consistent
 * error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  /**
   * Handles validation exceptions such as
   * {@link MethodArgumentNotValidException}.
   * 
   * @param e the {@link MethodArgumentNotValidException} thrown due to
   *          validation failure
   * @return a ResponseEntity containing a map of validation errors with field
   *         names as keys and error messages as values
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    logException(e);
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * Handles HTTP client errors such as {@link HttpClientErrorException}.
   * 
   * @param e the {@link HttpClientErrorException} thrown for client-related
   *          HTTP errors
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(HttpClientErrorException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles the {@link UserExistsException}.
   * 
   * @param e the {@link UserExistsException} thrown when a user already exists
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(UserExistsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleUserExistsException(UserExistsException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles the {@link ConfirmationTokenException}.
   * 
   * @param e the {@link ConfirmationTokenException} thrown when there is an
   *          issue with a confirmation token
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(ConfirmationTokenException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleConfirmationTokenException(ConfirmationTokenException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles the {@link MethodArgumentTypeMismatchException}.
   * 
   * @param e the {@link MethodArgumentTypeMismatchException} thrown when there is
   *          a type mismatch on conversion
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Map<String, String>> handleMethodArgTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles {@link UserNotFoundException}.
   * 
   * @param e the {@link UserNotFoundException} thrown when a user is not found
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
    return buildErrorResponse(e, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles {@link UsernameNotFoundException}.
   * 
   * @param e the {@link UsernameNotFoundException} thrown when a
   *          username is not found
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<Map<String, String>> handleUsernameNotFoundException(UsernameNotFoundException e) {
    return buildErrorResponse(e, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles authentication exceptions such as {@link AuthenticationException}.
   * 
   * @param e the {@link AuthenticationException} thrown when
   *          authentication fails
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException e) {
    return buildErrorResponse(e, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles {@link IllegalArgumentException}.
   * 
   * @param e the {@link IllegalArgumentException} thrown when an
   *          illegal argument is passed
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<Map<String, String>> handleIllegalArgException(IllegalArgumentException e) {
    return buildErrorResponse(e, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles JWT-related exceptions such as {@link JwtException}.
   * 
   * @param e the {@link JwtException} thrown when there is an issue
   *          with JWT authorization
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(JwtException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<Map<String, String>> handleJwtException(JwtException e) {
    return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles global exceptions such as any uncaught exceptions.
   * 
   * @param e the {@link Exception} thrown when an unexpected error
   *          occurs
   * @return a ResponseEntity containing a standardized error response
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Map<String, String>> handleGlobalException(Exception e) {
    return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Logs the exception details for error tracking and debugging purposes,
   * including the class name and message.
   * 
   * @param e the {@link Exception} that occurred
   */
  private void logException(Exception e) {
    log.error("{} occurred: {}", e.getClass().getSimpleName(), e.getMessage());
  }

  /**
   * Builds a standardized error response for the given exception and HTTP status.
   * 
   * @param e      the {@link Exception} that occurred
   * @param status the HTTP status to return
   * @return a ResponseEntity containing the error message, status, and timestamp
   */
  private ResponseEntity<Map<String, String>> buildErrorResponse(Exception e, HttpStatus status) {
    logException(e);
    Map<String, String> error = new HashMap<>();
    error.put("error", e.getMessage());
    error.put("status", status.toString());
    error.put("timestamp", String.valueOf(System.currentTimeMillis()));
    return ResponseEntity.status(status).body(error);
  }
}
