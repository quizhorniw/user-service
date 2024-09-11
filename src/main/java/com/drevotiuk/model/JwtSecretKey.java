package com.drevotiuk.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a secret key used for JWT encryption and decryption.
 * <p>
 * This class is mapped to a MongoDB collection defined by the
 * {@code secret-keys} placeholder.
 * It stores the ID and the encrypted key for JWT operations.
 * </p>
 */
@Document("secret_keys")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JwtSecretKey {
  /**
   * The unique identifier for the secret key.
   */
  @Id
  private String id;

  /**
   * The encrypted key used for JWT encryption and decryption.
   */
  private byte[] encryptedKey;
}
