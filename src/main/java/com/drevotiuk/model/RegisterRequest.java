package com.drevotiuk.model;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a request for user registration containing personal and account
 * information.
 * <p>
 * This class is used to encapsulate the registration details provided by a
 * user. It includes
 * validation constraints to ensure that all required fields are present and
 * meet specific criteria.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class RegisterRequest {
  /** The first name of the user. Cannot be blank. */
  @NotBlank(message = "First name is required")
  private String firstName;

  /** The last name of the user. Cannot be blank. */
  @NotBlank(message = "Last name is required")
  private String lastName;

  /**
   * The date of birth of the user. Must be a date in the past and cannot be null.
   */
  @NotNull(message = "Date of birth is required")
  @Past(message = "Date of birth must be in the past")
  private LocalDate dateOfBirth;

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
