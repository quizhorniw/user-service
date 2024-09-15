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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@Data
public class UserPrincipal implements UserDetails {
  /** Unique identifier for the user. */
  @Id
  @EqualsAndHashCode.Exclude
  private ObjectId id;

  /** The user's first name. */
  private String firstName;

  /** The user's last name. */
  private String lastName;

  /** The user's date of birth. */
  private LocalDate dateOfBirth;

  /**
   * The user's email address.
   * This field is indexed to facilitate fast lookups.
   */
  @Indexed
  private String email;

  /** The user's password. */
  private String password;

  /** The role assigned to the user. */
  private UserRole role;

  /** Indicates whether the user account is locked. */
  private boolean locked = false;

  /** Indicates whether the user account is enabled. */
  private boolean enabled = false;

  public UserPrincipal(RegisterRequest registerRequest) {
    this.firstName = registerRequest.getFirstName();
    this.lastName = registerRequest.getLastName();
    this.dateOfBirth = registerRequest.getDateOfBirth();
    this.email = registerRequest.getEmail();
    this.password = registerRequest.getPassword();
  }

  public UserPrincipal(LoginRequest loginRequest) {
    this.email = loginRequest.getEmail();
    this.password = loginRequest.getPassword();
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());
    return Collections.singleton(authority);
  }

  @Override
  public boolean isAccountNonLocked() {
    return !locked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }
}
