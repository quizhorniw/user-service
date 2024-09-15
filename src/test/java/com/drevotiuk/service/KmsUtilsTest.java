package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

import yandex.cloud.api.kms.v1.SymmetricCryptoServiceGrpc.SymmetricCryptoServiceBlockingStub;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceOuterClass.SymmetricDecryptResponse;
import yandex.cloud.api.kms.v1.SymmetricCryptoServiceOuterClass.SymmetricEncryptResponse;

@ExtendWith(MockitoExtension.class)
public class KmsUtilsTest {
  @Mock
  private SymmetricCryptoServiceBlockingStub symmetricCryptoServiceBlockingStub;
  private KmsUtils underTest;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    underTest = new KmsUtils(symmetricCryptoServiceBlockingStub);
    setDeclaredField(underTest, "keyId", "some-random-generated-value");
    setDeclaredField(underTest, "aad", "some-stored-aad-context");
  }

  @Test
  void shouldEncryptPlaintext() {
    // given
    byte[] plaintext = "test-plaintext".getBytes(StandardCharsets.UTF_8);
    byte[] ciphertext = "encrypted-data".getBytes(StandardCharsets.UTF_8);
    SymmetricEncryptResponse mockEncryptResponse = SymmetricEncryptResponse.newBuilder()
        .setCiphertext(ByteString.copyFrom(ciphertext))
        .build();

    given(symmetricCryptoServiceBlockingStub.encrypt(any())).willReturn(mockEncryptResponse);

    // when
    byte[] result = underTest.encrypt(plaintext);

    // then
    assertThat(result).isEqualTo(ciphertext);
  }

  @Test
  void shouldDecryptCiphertext() {
    // given
    byte[] ciphertext = "encrypted-data".getBytes(StandardCharsets.UTF_8);
    byte[] plaintext = "test-plaintext".getBytes(StandardCharsets.UTF_8);
    SymmetricDecryptResponse mockDecryptResponse = SymmetricDecryptResponse.newBuilder()
        .setPlaintext(ByteString.copyFrom(plaintext))
        .build();

    given(symmetricCryptoServiceBlockingStub.decrypt(any())).willReturn(mockDecryptResponse);

    // when
    byte[] result = underTest.decrypt(ciphertext);

    // then
    assertThat(result).isEqualTo(plaintext);
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
