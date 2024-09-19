package com.drevotiuk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drevotiuk.model.ConfirmationToken;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.exception.ConfirmationTokenException;
import com.drevotiuk.repository.ConfirmationTokenRepository;
import com.drevotiuk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ConfirmationTokenServiceTest {
  @Mock
  private ConfirmationTokenRepository confirmationTokenRepository;
  @Mock
  private UserRepository userRepository;
  private ConfirmationTokenService underTest;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    underTest = new ConfirmationTokenService(confirmationTokenRepository, userRepository);
    setDeclaredField(underTest, "tokenExpirationMinutes", 99);
  }

  @Test
  void shouldCreateAndSaveConfirmationTokenToDatabase() {
    // given
    String email = "testmail@mail.com";

    // when
    String result = underTest.create(email);

    // then
    ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor = ArgumentCaptor
        .forClass(ConfirmationToken.class);
    verify(confirmationTokenRepository).save(confirmationTokenArgumentCaptor.capture());

    ConfirmationToken capturedToken = confirmationTokenArgumentCaptor.getValue();
    assertThat(capturedToken.getToken()).isEqualTo(result);
    assertThat(capturedToken.getUserEmail()).isEqualTo(email);
  }

  @Test
  void shouldValidateConfirmationTokenViaDatabase() {
    // given
    String token = UUID.randomUUID().toString();
    ConfirmationToken confirmationToken = new ConfirmationToken(
        ObjectId.get(),
        token,
        LocalDateTime.now(),
        LocalDateTime.now().plusMinutes(99),
        false,
        "testmail@mail.com");
    given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));
    given(userRepository.findByEmail(confirmationToken.getUserEmail())).willReturn(Optional.of(new UserPrincipal()));

    // when
    String result = underTest.confirm(token);

    // then
    assertThat(result).isEqualTo("Email verified successfully");

    ArgumentCaptor<ConfirmationToken> confirmationTokenArgumentCaptor = ArgumentCaptor
        .forClass(ConfirmationToken.class);
    verify(confirmationTokenRepository).save(confirmationTokenArgumentCaptor.capture());

    ConfirmationToken capturedToken = confirmationTokenArgumentCaptor.getValue();
    confirmationToken.setActivated(true);
    assertThat(capturedToken).isEqualTo(confirmationToken);

    ArgumentCaptor<UserPrincipal> userPrincipalArgumentCaptor = ArgumentCaptor.forClass(UserPrincipal.class);
    verify(userRepository).save(userPrincipalArgumentCaptor.capture());

    UserPrincipal capturedUser = userPrincipalArgumentCaptor.getValue();
    assertThat(capturedUser.isEnabled()).isTrue();
  }

  @Test
  void shouldThrow_whenConfirmationTokenIsAlreadyActivated() {
    // given
    String token = UUID.randomUUID().toString();
    ConfirmationToken confirmationToken = new ConfirmationToken(
        ObjectId.get(),
        token,
        LocalDateTime.now(),
        LocalDateTime.now().plusMinutes(99),
        true,
        "testmail@mail.com");
    given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

    // when
    // then
    assertThatThrownBy(() -> underTest.confirm(token))
        .isInstanceOf(ConfirmationTokenException.class)
        .hasMessageContaining("Email is already verified");

    verify(confirmationTokenRepository, never()).save(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldThrow_whenConfirmationTokenIsExpired() {
    // given
    String token = UUID.randomUUID().toString();
    ConfirmationToken confirmationToken = new ConfirmationToken(
        ObjectId.get(),
        token,
        LocalDateTime.now(),
        LocalDateTime.now().minusMinutes(1),
        false,
        "testmail@mail.com");
    given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.of(confirmationToken));

    // when
    // then
    assertThatThrownBy(() -> underTest.confirm(token))
        .isInstanceOf(ConfirmationTokenException.class)
        .hasMessageContaining("Verification link is expired");

    verify(confirmationTokenRepository, never()).save(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldThrow_whenConfirmationTokenNotFound() {
    // given
    String token = UUID.randomUUID().toString();
    given(confirmationTokenRepository.findByToken(token)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> underTest.confirm(token))
        .isInstanceOf(ConfirmationTokenException.class)
        .hasMessageContaining("Invalid verification link");

    verify(confirmationTokenRepository, never()).save(any());
    verify(userRepository, never()).save(any());
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
