package com.drevotiuk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the details required for email verification.
 * <p>
 * This class contains information related to the email verification process,
 * including
 * the recipient's email address, their first name, and a verification link.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailVerificationDetails {
  /**
   * The email address of the recipient to be verified.
   */
  private String email;

  /**
   * The first name of the recipient.
   */
  private String firstName;

  /**
   * The verification link to be sent to the recipient.
   */
  private String link;
}
