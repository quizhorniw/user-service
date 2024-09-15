package com.drevotiuk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.model.UserView;
import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserManagementServiceTest {
  @Mock
  private UserRepository userRepository;
  private UserManagementService underTest;

  @BeforeEach
  void setUp() {
    underTest = new UserManagementService(userRepository);
  }

  @Test
  void shouldFindAllUsers() {
    // when
    underTest.findAll();

    // then
    verify(userRepository).findAll();
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

    // when
    UserView result = underTest.find(user.getId());

    // then
    assertThat(result).isEqualTo(new UserView(user));
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

    UserPrincipal toUpdate = new UserPrincipal();
    toUpdate.setFirstName("Johansen");
    toUpdate.setLastName("Dowgieh");

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
  void shouldThrow_whenUserNotFound_whileUpdating() {
    // given
    ObjectId userId = ObjectId.get();
    given(userRepository.findById(userId)).willReturn(Optional.empty());
    UserPrincipal mockUser = mock(UserPrincipal.class);

    // when
    // then
    assertThatThrownBy(() -> underTest.update(userId, mockUser))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldDeleteUserById() {
    // given
    ObjectId userId = ObjectId.get();

    // when
    underTest.delete(userId);

    // then
    verify(userRepository).deleteById(userId);
  }
}
