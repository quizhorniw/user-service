package com.drevotiuk.model;

/**
 * Enum representing the roles a user can have in the system.
 * <p>
 * This enum defines two types of user roles:
 * <ul>
 * <li>{@code USER} - Represents a regular user with standard access
 * rights.</li>
 * <li>{@code ADMIN} - Represents an administrator with elevated access
 * rights.</li>
 * </ul>
 */
public enum UserRole {
  /** Represents a regular user with standard access rights. */
  USER,

  /** Represents an administrator with elevated access rights. */
  ADMIN
}
