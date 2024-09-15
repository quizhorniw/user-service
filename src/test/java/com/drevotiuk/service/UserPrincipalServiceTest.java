package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserPrincipalServiceTest {
  @Mock
  private UserRepository userRepository;
  private UserPrincipalService underTest;

  @BeforeEach
  void setUp() {
    underTest = new UserPrincipalService(userRepository);
  }

  @Test
  void shouldLoadUserByUsername() {
    // given
    UserPrincipal user = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        false);
    given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));

    // when
    UserDetails result = underTest.loadUserByUsername(user.getEmail());

    // then
    assertThat(result).isEqualTo(user);
  }

  @Test
  void shouldThrow_whenUserNotFound() {
    // given
    String email = "johndoe@mail.com";
    given(userRepository.findByEmail(email)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> underTest.loadUserByUsername(email))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found");
  }
}
