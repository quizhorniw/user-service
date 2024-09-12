package com.drevotiuk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a view model for user details used in the application's user
 * interfaces.
 * This class is used to encapsulate user information for display purposes.
 * 
 * It includes fields for the user's first name, last name, email, and date of
 * birth.
 * The class provides constructors for creating instances based on individual
 * fields orfrom a {@link UserPrincipal} object.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserView {
  /** The user's first name. */
  private String firstName;

  /** The user's last name. */
  private String lastName;

  /** The user's email address. */
  private String email;

  /** The user's date of birth. */
  private String dateOfBirth;

  public UserView(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public UserView(UserPrincipal principal) {
    this.firstName = principal.getFirstName();
    this.lastName = principal.getLastName();
    this.dateOfBirth = principal.getDateOfBirth().toString();
    this.email = principal.getEmail();
  }
}
