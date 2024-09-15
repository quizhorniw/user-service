package com.drevotiuk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drevotiuk.model.JwtSecretKey;
import com.drevotiuk.model.exception.InvalidAlgorithmException;
import com.drevotiuk.repository.SecretKeyRepository;

@ExtendWith(MockitoExtension.class)
public class KeyManagementServiceTest {
  @Mock
  private KmsUtils kmsUtils;
  @Mock
  private SecretKeyRepository secretKeyRepository;
  private KeyManagementService underTest;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    underTest = new KeyManagementService(kmsUtils, secretKeyRepository);
    setDeclaredField(underTest, "keyId", "some-id");
  }

  @Test
  void shouldGiveEncryptedSecretKeyFromDatabase() {
    // given
    JwtSecretKey jwtSecretKey = new JwtSecretKey("some-id", "some-encrypted-key".getBytes());
    given(secretKeyRepository.findById("some-id")).willReturn(Optional.of(jwtSecretKey));

    // when
    byte[] result = underTest.getEncryptedSecretKey();

    // then
    assertThat(result).isEqualTo(jwtSecretKey.getEncryptedKey());
    verify(kmsUtils, never()).encrypt(any());
    verify(secretKeyRepository, never()).save(any());
  }

  @Test
  void shouldGenerateAndGiveEncryptedSecretKey() throws NoSuchFieldException, IllegalAccessException {
    // given
    given(secretKeyRepository.findById("some-id")).willReturn(Optional.empty());
    setDeclaredField(underTest, "algorithm", "HmacSHA256");
    given(kmsUtils.encrypt(any())).willReturn("generated-encrypted-key".getBytes());

    // when
    byte[] result = underTest.getEncryptedSecretKey();

    // then
    assertThat(result).isEqualTo("generated-encrypted-key".getBytes());

    ArgumentCaptor<JwtSecretKey> jwtSecretKeyArgumentCaptor = ArgumentCaptor.forClass(JwtSecretKey.class);
    verify(secretKeyRepository).save(jwtSecretKeyArgumentCaptor.capture());

    JwtSecretKey capturedKey = jwtSecretKeyArgumentCaptor.getValue();
    assertThat(capturedKey).isEqualTo(new JwtSecretKey("some-id", "generated-encrypted-key".getBytes()));
  }

  @Test
  void shouldThrow_whenAlgorithmIsInvalid() throws NoSuchFieldException, IllegalAccessException {
    // given
    given(secretKeyRepository.findById("some-id")).willReturn(Optional.empty());
    setDeclaredField(underTest, "algorithm", "some invalid algorithm");

    // when
    // then
    assertThatThrownBy(() -> underTest.getEncryptedSecretKey())
        .isInstanceOf(InvalidAlgorithmException.class)
        .hasMessageContaining("Algorithm not found");

    verify(secretKeyRepository, never()).save(any());
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
