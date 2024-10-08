package com.drevotiuk.service;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.UserView;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.exception.ForbiddenException;
import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing user-related operations.
 * Provides methods for retrieving, updating, and handling user information.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository repository;

  /**
   * Retrieves a user by their ID.
   *
   * @param userId the ID of the user to retrieve.
   * @return a {@link UserView} representing the user's details.
   */
  public UserView find(ObjectId userId) {
    log.info("Fetching user with ID {}", userId);
    UserPrincipal user = findPrincipalById(userId);
    validateAccess(user);
    return new UserView(user);
  }

  /**
   * Updates a user's details.
   *
   * @param userId  the ID of the user to update.
   * @param updated the updated user information.
   * @return a {@link UserView} representing the updated user's details.
   */
  public UserView update(ObjectId userId, UserView updated) {
    log.info("Updating user with ID {}", userId);
    UserPrincipal initialPrincipal = findPrincipalById(userId);
    validateAccess(initialPrincipal);
    updateFields(initialPrincipal, updated);
    repository.save(initialPrincipal);
    return new UserView(initialPrincipal);
  }

  /**
   * Handles a user request message from RabbitMQ and returns user information.
   *
   * @param userId the ID of the user to fetch.
   * @return a {@link UserView} containing the user's details if found;
   *         {@code null} otherwise.
   */
  @RabbitListener(queues = { "${rabbitmq.queue.user}" })
  public UserView handleUserRequest(String userId) {
    log.info("Received user request message; userID: {}", userId);
    if (!ObjectId.isValid(userId)) {
      log.warn("Invalid user ID format: {}", userId);
      return null; // Signals that something went wrong
    }

    try {
      return new UserView(findPrincipalById(new ObjectId(userId)));
    } catch (UserNotFoundException e) {
      return null;
    }
  }

  /**
   * Finds a user by their ID.
   *
   * @param userId the ID of the user to find.
   * @return the {@link UserPrincipal} corresponding to the provided ID.
   * @throws UserNotFoundException if no user with the given ID is found.
   */
  private UserPrincipal findPrincipalById(ObjectId userId) {
    return repository.findById(userId).orElseThrow(() -> {
      log.warn("User not found with ID: {}", userId);
      return new UserNotFoundException("User not found with ID: {}" + userId);
    });
  }

  /**
   * Validates user by their ID.
   *
   * @param user the accessed {@link UserPrincipal}.
   * @thrown ForbiddenException if not valid.
   */
  private void validateAccess(UserPrincipal user) {
    String askingEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    if (!askingEmail.equals(user.getEmail())) {
      log.warn("Access forbidden for user with email: {}", askingEmail);
      throw new ForbiddenException("Access forbidden");
    }
  }

  /**
   * Updates the fields of a user with the provided information.
   *
   * @param initial the original user principal to be updated.
   * @param updated the new user information to apply.
   */
  private void updateFields(UserPrincipal initial, UserView updated) {
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
