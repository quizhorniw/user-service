package com.drevotiuk.model;

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
 * fields or
 * from a {@link UserPrincipal} object.
 */
@NoArgsConstructor
@Data
public class UserView {

  /**
   * The user's first name.
   */
  private String firstName;

  /**
   * The user's last name.
   */
  private String lastName;

  /**
   * The user's email address.
   */
  private String email;

  /**
   * The user's date of birth.
   */
  private String dateOfBirth;

  /**
   * Constructs a {@link UserView} with the specified first name and last name.
   * 
   * @param firstName the first name of the user.
   * @param lastName  the last name of the user.
   */
  public UserView(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  /**
   * Constructs a {@link UserView} using the details from the specified
   * {@link UserPrincipal}.
   * 
   * @param principal the {@link UserPrincipal} object containing user details.
   */
  public UserView(UserPrincipal principal) {
    this.firstName = principal.getFirstName();
    this.lastName = principal.getLastName();
    this.dateOfBirth = principal.getDateOfBirth().toString();
    this.email = principal.getEmail();
  }
}
