package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.drevotiuk.model.EmailVerificationDetails;
import com.drevotiuk.model.LoginRequest;
import com.drevotiuk.model.RegisterRequest;
import com.drevotiuk.model.UserHeaders;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.model.exception.UserExistsException;
import com.drevotiuk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private JwtService jwtService;
  @Mock
  private ConfirmationTokenService confirmationTokenService;
  @Mock
  private BCryptPasswordEncoder passwordEncoder;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private RabbitTemplate rabbitTemplate;
  private AuthService underTest;

  @BeforeEach
  void setUp() {
    underTest = new AuthService(userRepository, jwtService, confirmationTokenService, passwordEncoder,
        authenticationManager, rabbitTemplate);
  }

  @Test
  void shouldRegisterNewUserBasedOnRequest() throws NoSuchFieldException, IllegalAccessException {
    // given
    RegisterRequest request = new RegisterRequest(
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123");

    given(userRepository.existsByEmail("johndoe@mail.com")).willReturn(false);

    UserPrincipal expectedUser = new UserPrincipal(request);
    expectedUser.setId(ObjectId.get());
    expectedUser.setPassword("encoded password");
    expectedUser.setRole(UserRole.USER);

    given(passwordEncoder.encode("qwerty123")).willReturn("encoded password");
    given(confirmationTokenService.create("johndoe@mail.com")).willReturn("verif-token");
    setDeclaredField(underTest, "serviceUrl", "http://mysite.com/myservice");
    setDeclaredField(underTest, "exchange", "test exchange");
    setDeclaredField(underTest, "routingKey", "test routing key");

    // when
    String result = underTest.register(request);

    // then
    assertThat(result).isEqualTo("Verification link was sent to email johndoe@mail.com");

    ArgumentCaptor<UserPrincipal> userPrincipalArgumentCaptor = ArgumentCaptor
        .forClass(UserPrincipal.class);
    verify(userRepository).save(userPrincipalArgumentCaptor.capture());

    UserPrincipal capturedUser = userPrincipalArgumentCaptor.getValue();
    assertThat(capturedUser).isEqualTo(expectedUser);

    EmailVerificationDetails details = new EmailVerificationDetails(
        "johndoe@mail.com",
        "John",
        "http://mysite.com/myservice/users/confirm?token=verif-token");
    verify(rabbitTemplate).convertAndSend(
        "test exchange",
        "test routing key",
        details);
  }

  @Test
  void shouldThrow_whenUserAlreadyExists() {
    // given
    RegisterRequest request = new RegisterRequest(
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123");

    given(userRepository.existsByEmail("johndoe@mail.com")).willReturn(true);

    // when
    // then
    assertThatThrownBy(() -> underTest.register(request))
        .isInstanceOf(UserExistsException.class)
        .hasMessageContaining("User already exists");

    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any());
    verify(rabbitTemplate, never()).convertAndSend(
        eq("test exchange"),
        eq("test routing key"),
        any(EmailVerificationDetails.class));
  }

  @Test
  void shouldLoginUserBasedOnRequest() {
    // given
    LoginRequest request = new LoginRequest("johndoe@mail.com", "qwerty123");

    // when
    underTest.login(request);

    // then
    verify(authenticationManager).authenticate(
        new UsernamePasswordAuthenticationToken("johndoe@mail.com", "qwerty123"));
    verify(jwtService).generateToken("johndoe@mail.com");
  }

  @Test
  void shouldThrow_whenAuthenticationFails_whileLoggingIn() {
    // given
    LoginRequest request = new LoginRequest("johndoe@mail.com", "qwerty123");
    given(authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken("johndoe@mail.com", "qwerty123")))
        .willThrow(BadCredentialsException.class);

    // when
    // then
    assertThatThrownBy(() -> underTest.login(request))
        .isInstanceOf(BadCredentialsException.class);

    verify(jwtService, never()).generateToken("johndoe@mail.com");
  }

  @Test
  void shouldAuthorizeBasedOnCredentials() throws NoSuchFieldException, IllegalAccessException {
    // given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    given(authentication.getName()).willReturn("johndoe@mail.com");

    UserPrincipal user = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        true);

    given(userRepository.findByEmail("johndoe@mail.com")).willReturn(Optional.of(user));
    setDeclaredField(underTest, "userIdHeader", "Test UserID Header");
    setDeclaredField(underTest, "userRoleHeader", "Test UserRole Header");

    // when
    UserHeaders result = underTest.authorize();

    // then
    assertThat(result.getHeaders())
        .containsEntry("Test UserID Header", user.getId().toString())
        .containsEntry("Test UserRole Header", user.getRole().name());
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
