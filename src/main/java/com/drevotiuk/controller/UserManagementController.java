package com.drevotiuk.controller;

import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserView;
import com.drevotiuk.service.UserManagementService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users in the system.
 * Provides endpoints for creating, retrieving, updating, and deleting user
 * information.
 */
@RestController
@RequestMapping("/api/${api.version}/management/users")
@RequiredArgsConstructor
public class UserManagementController {
  private final UserManagementService service;

  /**
   * Retrieves all users.
   *
   * @return ResponseEntity containing a list of all UserView objects.
   */
  @GetMapping
  public ResponseEntity<List<UserView>> findAll() {
    List<UserView> users = service.findAll();
    return ResponseEntity.ok(users);
  }

  /**
   * Retrieves a specific user by their ID.
   *
   * @param userId the ID of the user to retrieve.
   * @return ResponseEntity containing the UserView object of the requested user.
   */
  @GetMapping("/{userId}")
  public ResponseEntity<UserView> find(@PathVariable ObjectId userId) {
    UserView user = service.find(userId);
    return ResponseEntity.ok(user);
  }

  /**
   * Updates a user identified by their ID with new information.
   *
   * @param userId           the ID of the user to update.
   * @param updatedPrincipal the UserPrincipal object containing the updated user
   *                         information.
   * @return ResponseEntity containing the updated UserView object.
   */
  @PutMapping("/{userId}")
  public ResponseEntity<UserView> update(
      @PathVariable ObjectId userId, @RequestBody UserPrincipal updatedPrincipal) {
    UserView updatedUser = service.update(userId, updatedPrincipal);
    return ResponseEntity.ok(updatedUser);
  }

  /**
   * Deletes a user identified by their ID.
   *
   * @param userId the ID of the user to delete.
   * @return ResponseEntity with no content if the deletion was successful.
   */
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable ObjectId userId) {
    service.delete(userId);
    return ResponseEntity.noContent().build();
  }
}
