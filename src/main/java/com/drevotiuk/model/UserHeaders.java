package com.drevotiuk.model;

import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A class for storing security-related headers used for authorization in other
 * services through the gateway.
 * This class holds a map of headers, such as UserID and UserRole, that are
 * essential for user authentication
 * and authorization in a microservice architecture.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserHeaders {

  /**
   * A map containing security-related headers where the key is the header name
   * and the value is the header's value.
   */
  private Map<String, String> headers;
}
