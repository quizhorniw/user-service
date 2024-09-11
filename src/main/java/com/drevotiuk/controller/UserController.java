package com.drevotiuk.controller;

import com.drevotiuk.model.UserView;

import com.drevotiuk.model.LoginRequest;
import com.drevotiuk.model.RegisterRequest;
import com.drevotiuk.model.UserHeaders;
import com.drevotiuk.service.AuthService;
import com.drevotiuk.service.ConfirmationTokenService;
import com.drevotiuk.service.UserService;

import lombok.RequiredArgsConstructor;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing user-related operations.
 * Handles user registration, login, profile updates, and token confirmation.
 */
@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final AuthService authService;
  private final ConfirmationTokenService tokenService;

  /**
   * Registers a new user.
   *
   * @param request the registration details for the new user.
   * @return a ResponseEntity containing a confirmation message.
   */
  @PostMapping("/register")
  public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
    String response = authService.register(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Confirms a user's email address using a confirmation token.
   *
   * @param token the confirmation token.
   * @return a ResponseEntity containing a confirmation message.
   */
  @GetMapping("/confirm")
  public ResponseEntity<String> confirmToken(@RequestParam String token) {
    String response = tokenService.confirm(token);
    return ResponseEntity.ok(response);
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param user the login request containing user credentials.
   * @return a ResponseEntity containing the JWT token.
   */
  @PostMapping("/login")
  public ResponseEntity<String> login(@Valid @RequestBody LoginRequest user) {
    String token = authService.login(user);
    return ResponseEntity.ok(token);
  }

  /**
   * Authorizes a user and retrieves user headers.
   *
   * @return a ResponseEntity containing the user headers.
   */
  @GetMapping("/auth")
  public ResponseEntity<UserHeaders> authorize() {
    UserHeaders headers = authService.authorize();
    return ResponseEntity.ok(headers);
  }

  /**
   * Retrieves the profile information of a user by their ID.
   *
   * @param userId the ID of the user to retrieve.
   * @return a ResponseEntity containing the user's profile information.
   */
  @GetMapping("/profile/{userId}")
  public ResponseEntity<UserView> find(@PathVariable ObjectId userId) {
    UserView user = userService.find(userId);
    return ResponseEntity.ok(user);
  }

  /**
   * Updates the profile information of a user.
   *
   * @param userId  the ID of the user to update.
   * @param updated the updated user profile information.
   * @return a ResponseEntity containing the updated user's profile information.
   */
  @PutMapping("/profile/{userId}")
  public ResponseEntity<UserView> update(@PathVariable ObjectId userId, @RequestBody UserView updated) {
    UserView updatedUser = userService.update(userId, updated);
    return ResponseEntity.ok(updatedUser);
  }
}
