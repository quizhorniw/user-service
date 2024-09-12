package com.drevotiuk.model;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a confirmation token used for email verification.
 * This class maps to the "confirmation_tokens" collection in MongoDB.
 * It includes fields for storing the token value, issuance and expiration
 * times,
 * activation status, and the associated user's email.
 */
@Document("confirmation_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfirmationToken {
  /** Unique identifier for the confirmation token. */
  @Id
  private ObjectId id;

  /**
   * The confirmation token value.
   * This token is used to verify the user's email address.
   */
  @Indexed
  private String token;

  /** The date and time when the token was issued. */
  private LocalDateTime issuedAt;

  /** The date and time when the token expires. */
  private LocalDateTime expiredAt;

  /**
   * Indicates whether the token has been activated (i.e., used for email
   * verification).
   */
  private boolean activated;

  /** The email address of the user associated with the token. */
  private String userEmail;
}
