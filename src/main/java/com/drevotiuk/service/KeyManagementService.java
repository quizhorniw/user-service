package com.drevotiuk.service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.JwtSecretKey;
import com.drevotiuk.model.exception.InvalidAlgorithmException;
import com.drevotiuk.model.exception.KeyNotFoundException;
import com.drevotiuk.repository.SecretKeyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing cryptographic keys used for JWT operations.
 * Provides methods for generating, encrypting, and retrieving secret keys from
 * the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeyManagementService {
  @Value("${security.jwt.secret-key.algorithm}")
  private String algorithm;
  @Value("${JWT_KEY_ID}")
  private String keyId;

  private final KmsUtils kmsUtils;
  private final SecretKeyRepository secretKeyRepository;

  /**
   * Retrieves the encrypted secret key. If the key is not found in the database,
   * a new key is generated, encrypted, and stored in the database.
   * 
   * @return the encrypted secret key.
   */
  public byte[] getEncryptedSecretKey() {
    try {
      return retrieveKeyFromDatabase();
    } catch (KeyNotFoundException e) {
      return generateAndStoreKey();
    }
  }

  /**
   * Generates secret key as plain text using {@link KeyGenerator}.
   * 
   * @return the generated secret key.
   */
  private byte[] generateSecretKey() {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
      return Base64.getEncoder().encode(keyGen.generateKey().getEncoded());
    } catch (NoSuchAlgorithmException e) {
      log.warn("Algorithm not found: {}", algorithm, e);
      // Runtime exception to be caught in @RestControllerAdvice to stop application
      // from future data proceeding
      throw new InvalidAlgorithmException("Algorithm not found");
    }
  }

  /**
   * Generates secret key as plain text and stores it in the database.
   * 
   * @return the encrypted generated secret key.
   */
  private byte[] generateAndStoreKey() {
    log.info("Generating and encrypting new secret key");
    byte[] encryptedSecretKey = kmsUtils.encrypt(generateSecretKey());
    storeKeyToDatabase(encryptedSecretKey);
    return encryptedSecretKey;
  }

  /**
   * Retrieves the encrypted secret key from the database using the provided key
   * ID.
   * 
   * @return the encrypted secret key if found in the database.
   * @throws KeyNotFoundException if the secret key is not found in the database.
   */
  private byte[] retrieveKeyFromDatabase() throws KeyNotFoundException {
    log.info("Extracting encrypted secret key from database");
    return secretKeyRepository.findById(keyId)
        .map(JwtSecretKey::getEncryptedKey)
        .orElseThrow(() -> {
          log.info("Secret key not found in database, creating one");
          return new KeyNotFoundException("Secret key not found in database");
        });
  }

  /**
   * Stores the provided encrypted secret key in the database.
   * 
   * @param secretKey the encrypted secret key to be stored.
   */
  private void storeKeyToDatabase(byte[] secretKey) {
    JwtSecretKey key = new JwtSecretKey(keyId, secretKey);
    log.info("Saving encrypted secret key to database");
    secretKeyRepository.save(key);
  }
}
