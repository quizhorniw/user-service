package com.drevotiuk.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drevotiuk.model.ConfirmationToken;
import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.exception.ConfirmationTokenException;
import com.drevotiuk.repository.ConfirmationTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling confirmation tokens used for email verification.
 * Provides methods for creating, validating, and confirming tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationTokenService {
  @Value("${security.confirmation-token.expiration}")
  private long tokenExpirationMinutes;

  private final ConfirmationTokenRepository repository;
  private final AuthService authService;

  /**
   * Creates a new confirmation token for the given user principal.
   * 
   * @param principal the user principal for whom the token is created.
   * @return the generated confirmation token.
   */
  public String create(UserPrincipal principal) {
    String token = UUID.randomUUID().toString();
    ConfirmationToken confirmationToken = build(principal, token);
    save(confirmationToken);
    log.info("Created confirmation token: {}", token);
    return token;
  }

  /**
   * Confirms the provided token, activates the associated user, and returns a
   * success message.
   * 
   * @param token the confirmation token to be validated and confirmed.
   * @return a success message indicating the email was verified successfully.
   * @throws ConfirmationTokenException if the token is invalid, expired, or
   *                                    already activated.
   */
  public String confirm(String token) {
    log.info("Token confirmation: {}", token);
    ConfirmationToken confirmationToken = find(token);
    validate(confirmationToken);
    activate(confirmationToken);
    authService.enableUser(confirmationToken.getUserEmail());
    log.info("Email verified successfully for token: {}", token);
    return "Email verified successfully";
  }

  /**
   * Finds a confirmation token by its token value.
   * 
   * @param token the token value to find.
   * @return the {@link ConfirmationToken} associated with the given token value.
   * @throws ConfirmationTokenException if the token is not found.
   */
  private ConfirmationToken find(String token) {
    return repository.findByToken(token).orElseThrow(() -> {
      log.warn("Token was not found: {}", token);
      return new ConfirmationTokenException("Invalid verification link");
    });
  }

  /**
   * Builds a new confirmation token for the given user principal.
   * 
   * @param principal the user principal for whom the token is created.
   * @param token     the token value.
   * @return a new {@link ConfirmationToken} instance.
   */
  private ConfirmationToken build(UserPrincipal principal, String token) {
    return new ConfirmationToken(
        ObjectId.get(),
        token,
        LocalDateTime.now(),
        LocalDateTime.now().plusMinutes(tokenExpirationMinutes),
        false,
        principal.getEmail());
  }

  /**
   * Validates the given confirmation token to ensure it is not expired or already
   * activated.
   * 
   * @param token the confirmation token to validate.
   * @throws ConfirmationTokenException if the token is already activated or
   *                                    expired.
   */
  private void validate(ConfirmationToken token) {
    if (token.isActivated()) {
      log.warn("Email for token {} is already verified", token.getToken());
      throw new ConfirmationTokenException("Email is already verified");
    }
    if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
      log.warn("Token {} is expired", token.getToken());
      throw new ConfirmationTokenException("Verification link is expired");
    }
  }

  /**
   * Activates the given confirmation token and saves it to the database.
   * 
   * @param token the confirmation token to activate.
   */
  private void activate(ConfirmationToken token) {
    token.setActivated(true);
    log.info("Token {} activated", token.getToken());
    save(token);
  }

  /**
   * Saves the given confirmation token to the database.
   * 
   * @param token the confirmation token to save.
   */
  private void save(ConfirmationToken token) {
    log.info("Saving token: {}", token.getToken());
    repository.save(token);
  }
}
