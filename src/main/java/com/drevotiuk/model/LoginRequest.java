package com.drevotiuk.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a request for user login containing email and password.
 * <p>
 * This class is used to encapsulate the login credentials provided by a user
 * during authentication.
 * It includes validation constraints to ensure that the email and password meet
 * the required criteria.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class LoginRequest {
  /**
   * The email address of the user. Must be a valid email format and cannot be
   * blank.
   */
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  private String email;

  /**
   * The password for the user. Must be at least 8 characters long and cannot be
   * blank.
   */
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Minimal length of password is 8 characters")
  private String password;
}
