package com.drevotiuk.service;

import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserView;
import com.drevotiuk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user information.
 * Provides methods for retrieving, updating, and deleting user data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {
  private final UserRepository repository;

  /**
   * Fetches all users and returns them as a list of UserView objects.
   *
   * @return List of {@link UserView} representing all users.
   */
  public List<UserView> findAll() {
    List<UserView> users = repository.findAll().stream()
        .map(UserView::new)
        .collect(Collectors.toList());
    log.info("Fetched {} users", users.size());
    return users;
  }

  /**
   * Fetches a user by their ID.
   *
   * @param userId the ID of the user to fetch.
   * @return {@link UserView} representing the user.
   */
  public UserView find(ObjectId userId) {
    log.info("Fetching principal with ID {}", userId);
    return new UserView(findById(userId));
  }

  /**
   * Updates a user identified by their ID with new values.
   *
   * @param userId           the ID of the user to update.
   * @param updatedPrincipal the updated user data.
   * @return {@link UserView} representing the updated user.
   */
  public UserView update(ObjectId userId, UserPrincipal updatedPrincipal) {
    log.info("Updating user with ID {}", userId);
    UserPrincipal initialPrincipal = findById(userId);
    updateUserFields(initialPrincipal, updatedPrincipal);
    repository.save(initialPrincipal);
    return new UserView(initialPrincipal);
  }

  /**
   * Deletes a user by their ID.
   *
   * @param userId the ID of the user to delete.
   */
  public void delete(ObjectId userId) {
    log.info("Deleting user with ID {}", userId);
    repository.deleteById(userId);
  }

  /**
   * Finds a user by their ID.
   *
   * @param userId the ID of the user to find.
   * @return {@link UserPrincipal} representing the user.
   */
  private UserPrincipal findById(ObjectId userId) {
    return repository.findById(userId).orElseThrow(() -> {
      log.warn("User with ID {} not found", userId);
      return new UserNotFoundException("User not found with ID: " + userId);
    });
  }

  /**
   * Updates the fields of an existing user with the values from the updated user.
   *
   * @param initial the initial user to be updated.
   * @param updated the updated user containing new values.
   */
  private void updateUserFields(UserPrincipal initial, UserPrincipal updated) {
    Optional.ofNullable(updated.getFirstName())
        .filter(firstName -> !firstName.isEmpty())
        .ifPresent(firstName -> {
          log.info("Updated first name for user with ID {}: {}", initial.getId(), firstName);
          initial.setFirstName(firstName);
        });

    Optional.ofNullable(updated.getLastName())
        .filter(lastName -> !lastName.isEmpty())
        .ifPresent(lastName -> {
          log.info("Updated last name for user with ID {}: {}", initial.getId(), lastName);
          initial.setLastName(lastName);
        });
  }
}
