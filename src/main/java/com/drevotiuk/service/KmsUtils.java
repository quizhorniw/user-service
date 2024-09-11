package com.drevotiuk.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

import lombok.RequiredArgsConstructor;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceGrpc.SymmetricCryptoServiceBlockingStub;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceOuterClass.SymmetricDecryptRequest;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceOuterClass.SymmetricEncryptRequest;

/**
 * Utility class for encryption and decryption using a symmetric key service via
 * Yandex KMS.
 * Provides methods to encrypt and decrypt text using a specified symmetric key
 * and AAD.
 */
@Service
@RequiredArgsConstructor
public class KmsUtils {
  @Value("${SYMMETRIC_KEY_ID}")
  private String keyId;
  @Value("${AAD_CONTEXT}")
  private String aad;

  private final SymmetricCryptoServiceBlockingStub symmetricCryptoService;

  /**
   * Encrypts the given plaintext using the symmetric key and AAD.
   *
   * @param plaintext the plaintext to encrypt.
   * @return the encrypted ciphertext.
   */
  public byte[] encrypt(byte[] plaintext) {
    return symmetricCryptoService.encrypt(SymmetricEncryptRequest.newBuilder()
        .setKeyId(keyId)
        .setPlaintext(ByteString.copyFrom(plaintext))
        .setAadContext(ByteString.copyFrom(aad, StandardCharsets.UTF_8))
        .build()).getCiphertext().toByteArray();
  }

  /**
   * Decrypts the given ciphertext using the symmetric key and AAD.
   *
   * @param ciphertext the ciphertext to decrypt.
   * @return the decrypted plaintext.
   */
  public byte[] decrypt(byte[] ciphertext) {
    return symmetricCryptoService.decrypt(SymmetricDecryptRequest.newBuilder()
        .setKeyId(keyId)
        .setCiphertext(ByteString.copyFrom(ciphertext))
        .setAadContext(ByteString.copyFrom(aad, StandardCharsets.UTF_8))
        .build()).getPlaintext().toByteArray();
  }
}
