package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.model.UserView;
import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private Authentication authentication;
  @Mock
  private SecurityContext securityContext;
  private UserService underTest;

  @BeforeEach
  void setUp() {
    underTest = new UserService(userRepository);
  }

  @Test
  void shouldFindUserById() {
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
        true);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    given(authentication.getName()).willReturn(user.getEmail());

    // when
    UserView found = underTest.find(user.getId());

    // then
    assertThat(found).isEqualTo(new UserView(user));
  }

  @Test
  void shouldThrow_whenDidNotValidateUser() {
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
        true);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    given(authentication.getName()).willReturn("some another auth name, will raise exception");

    // when
    // then
    assertThatThrownBy(() -> underTest.find(user.getId()))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Access forbidden");
  }

  @Test
  void shouldThrow_whenUserNotFound() {
    // given
    ObjectId userId = ObjectId.get();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> underTest.find(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void shouldUpdateUserById() {
    // given
    UserPrincipal initial = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        true);
    given(userRepository.findById(initial.getId())).willReturn(Optional.of(initial));

    UserView toUpdate = new UserView("Johansen", "Dowee");

    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    given(authentication.getName()).willReturn(initial.getEmail());

    // when
    UserView result = underTest.update(initial.getId(), toUpdate);

    // then
    assertThat(result.getFirstName()).isEqualTo(toUpdate.getFirstName());
    assertThat(result.getLastName()).isEqualTo(toUpdate.getLastName());

    ArgumentCaptor<UserPrincipal> userPrincipalArgumentCaptor = ArgumentCaptor.forClass(UserPrincipal.class);
    verify(userRepository).save(userPrincipalArgumentCaptor.capture());

    UserPrincipal capturedUser = userPrincipalArgumentCaptor.getValue();
    initial.setFirstName(toUpdate.getFirstName());
    initial.setLastName(toUpdate.getLastName());
    assertThat(capturedUser).isEqualTo(initial);
  }

  @Test
  void shouldThrow_whenDidNotValidateUser_whileUpdating() {
    // given
    UserPrincipal initial = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        true);
    given(userRepository.findById(initial.getId())).willReturn(Optional.of(initial));
    UserView mockUserView = mock(UserView.class);

    given(securityContext.getAuthentication()).willReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    given(authentication.getName()).willReturn("some another auth name, will raise exception");

    // when
    // then
    assertThatThrownBy(() -> underTest.update(initial.getId(), mockUserView))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Access forbidden");
  }

  @Test
  void shouldThrow_whenUserNotFound_whileUpdating() {
    // given
    ObjectId userId = ObjectId.get();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when
    // then
    assertThatThrownBy(() -> underTest.update(userId, new UserView()))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  void shouldHandleUserRabbitMqRequest() {
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
        true);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    UserView result = underTest.handleUserRequest(user.getId().toString());

    // then
    assertThat(result).isEqualTo(new UserView(user));
  }

  @Test
  void shouldReturnNull_whenUserNotFound_whileHandlingUserRequest() {
    // given
    ObjectId userId = ObjectId.get();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when
    UserView result = underTest.handleUserRequest(userId.toString());

    // then
    assertThat(result).isNull();
  }

  @Test
  void shouldReturnNull_whenUserIdIsInvalid() {
    // given
    String userId = "invalid ObjectId";

    // when
    UserView result = underTest.handleUserRequest(userId);

    // then
    assertThat(result).isNull();
  }
}
