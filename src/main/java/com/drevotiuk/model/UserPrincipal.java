package com.drevotiuk.model;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user in the system with details required for authentication and
 * authorization.
 * This class maps to the "users" collection in MongoDB and implements
 * {@link UserDetails}.
 * It includes user information such as name, date of birth, email, password,
 * and role.
 */
@Document("users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserPrincipal implements UserDetails {
  /**
   * Unique identifier for the user.
   */
  @Id
  private ObjectId id;

  /**
   * The user's first name.
   */
  private String firstName;

  /**
   * The user's last name.
   */
  private String lastName;

  /**
   * The user's date of birth.
   */
  private LocalDate dateOfBirth;

  /**
   * The user's email address.
   * This field is indexed to facilitate fast lookups.
   */
  @Indexed
  private String email;

  /**
   * The user's password.
   */
  private String password;

  /**
   * The role assigned to the user.
   */
  private UserRole role;

  /**
   * Indicates whether the user account is locked.
   */
  private boolean locked = false;

  /**
   * Indicates whether the user account is enabled.
   */
  private boolean enabled = false;

  /**
   * Constructs a {@link UserPrincipal} using the provided
   * {@link RegisterRequest}.
   * 
   * @param registerRequest the registration request containing user details.
   */
  public UserPrincipal(RegisterRequest registerRequest) {
    this.firstName = registerRequest.getFirstName();
    this.lastName = registerRequest.getLastName();
    this.dateOfBirth = registerRequest.getDateOfBirth();
    this.email = registerRequest.getEmail();
    this.password = registerRequest.getPassword();
  }

  /**
   * Constructs a {@link UserPrincipal} using the provided {@link LoginRequest}.
   * 
   * @param loginRequest the login request containing email and password.
   */
  public UserPrincipal(LoginRequest loginRequest) {
    this.email = loginRequest.getEmail();
    this.password = loginRequest.getPassword();
  }

  /**
   * Returns the username of the user, which is the email address.
   * 
   * @return the email address of the user.
   */
  @Override
  public String getUsername() {
    return email;
  }

  /**
   * Returns the authorities granted to the user.
   * 
   * @return a collection of {@link GrantedAuthority} representing the user's
   *         role.
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
    return Collections.singleton(authority);
  }

  /**
   * Indicates whether the user account is non-locked.
   * 
   * @return {@code true} if the account is not locked; {@code false} otherwise.
   */
  @Override
  public boolean isAccountNonLocked() {
    return !locked;
  }

  /**
   * Indicates whether the user's credentials are non-expired.
   * 
   * @return {@code true} as credentials are always considered valid in this
   *         implementation.
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user account is non-expired.
   * 
   * @return {@code true} as accounts are always considered valid in this
   *         implementation.
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }
}
