package com.drevotiuk.service;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.EmailVerificationDetails;
import com.drevotiuk.model.LoginRequest;
import com.drevotiuk.model.RegisterRequest;
import com.drevotiuk.model.UserHeaders;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.model.exception.UserExistsException;
import com.drevotiuk.model.exception.UserNotFoundException;
import com.drevotiuk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling authentication and authorization operations,
 * including user registration, login, and token management.
 * Provides methods for user registration, email verification, user enabling,
 * login, and retrieving user headers for authorization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  @Value("${services.user.gateway-uri}")
  private String serviceUrl;
  @Value("${rabbitmq.exchange.notification-service}")
  private String exchange;
  @Value("${rabbitmq.routingkey.email-verification}")
  private String routingKey;

  @Value("${security.header.id}")
  private String userIdHeader;
  @Value("${security.header.role}")
  private String userRoleHeader;

  @Lazy
  @Autowired
  private ConfirmationTokenService confirmationTokenService;

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RabbitTemplate rabbitTemplate;

  /**
   * Registers a new user with the given registration request.
   * 
   * @param request the registration request containing user details.
   * @return a confirmation message indicating that a verification link has been
   *         sent.
   * @throws UserExistsException if a user with the given email already exists.
   */
  public String register(RegisterRequest request) {
    String email = request.getEmail();
    log.info("Registering user with email {}", email);

    if (userRepository.existsByEmail(email)) {
      handleUserExists(email);
    }

    UserPrincipal principal = createPrincipal(request);
    userRepository.save(principal);
    handleVerificationEmail(principal);

    return "Verification link was sent to email " + email;
  }

  /**
   * Enables the user with the specified email.
   * 
   * @param email the email of the user to enable.
   */
  public void enableUser(String email) {
    UserPrincipal principal = getUserByEmail(email);
    principal.setEnabled(true);
    userRepository.save(principal);
  }

  /**
   * Logs in a user with the given login request and generates a JWT token.
   * 
   * @param request the login request containing user credentials.
   * @return a JWT token for the authenticated user.
   * @throws AuthenticationException if authentication fails.
   */
  public String login(LoginRequest request) {
    String email = request.getEmail();
    log.info("Logging in user with email {}", email);
    authenticateUser(request);
    return jwtService.generateToken(email);
  }

  /**
   * Retrieves user headers for the currently authenticated user.
   * 
   * @return a {@link UserHeaders} object containing the user's ID and role.
   */
  public UserHeaders authorize() {
    String email = getAuthenticatedEmail();
    UserPrincipal principal = getUserByEmail(email);

    Map<String, String> headers = new HashMap<>();
    headers.put(userIdHeader, principal.getId().toString());
    headers.put(userRoleHeader, principal.getRole().name());

    return new UserHeaders(headers);
  }

  /**
   * Handles the case where a user with the given email already exists.
   * 
   * @param email the email of the user that already exists.
   * @throws UserExistsException if a user with the given email already exists.
   */
  private void handleUserExists(String email) {
    log.warn("User with email {} already exists, creation aborted", email);
    throw new UserExistsException(String.format("User with email %s already exists", email));
  }

  /**
   * Creates a {@link UserPrincipal} object from the registration request.
   * 
   * @param request the registration request containing user details.
   * @return a {@link UserPrincipal} object with the provided details.
   */
  private UserPrincipal createPrincipal(RegisterRequest request) {
    UserPrincipal principal = new UserPrincipal(request);
    principal.setId(ObjectId.get());
    principal.setPassword(passwordEncoder.encode(principal.getPassword()));
    principal.setRole(UserRole.USER);
    return principal;
  }

  /**
   * Generates a verification link for the provided token.
   * 
   * @param token the verification token.
   * @return a formatted verification link.
   */
  private String generateVerificationLink(String token) {
    return String.format("%s/users/confirm?token=%s", serviceUrl, token);
  }

  /**
   * Handles sending a verification email to the user.
   * 
   * @param principal the user principal containing user details.
   */
  private void handleVerificationEmail(UserPrincipal principal) {
    String token = confirmationTokenService.create(principal);
    String link = generateVerificationLink(token);
    sendVerificationEmail(new EmailVerificationDetails(principal.getEmail(), principal.getFirstName(), link));
  }

  /**
   * Sends a verification email using RabbitMQ.
   * 
   * @param details the email verification details to be sent.
   */
  private void sendVerificationEmail(EmailVerificationDetails details) {
    log.info("Sending email verification message: {}", details);
    rabbitTemplate.convertAndSend(exchange, routingKey, details);
  }

  /**
   * Retrieves the user by email.
   * 
   * @param email the email of the user.
   * @return the {@link UserPrincipal} object associated with the email.
   * @throws UserNotFoundException if no user is found with the given email.
   */
  private UserPrincipal getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElseThrow(() -> {
      log.warn("User not found with email {}", email);
      return new UserNotFoundException("User not found with email " + email);
    });
  }

  /**
   * Authenticates a user based on the provided login request.
   * 
   * @param request the login request containing user credentials.
   * @throws AuthenticationException if authentication fails.
   */
  private void authenticateUser(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
  }

  /**
   * Retrieves the email of the currently authenticated user.
   * 
   * @return the email of the authenticated user.
   */
  private String getAuthenticatedEmail() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
