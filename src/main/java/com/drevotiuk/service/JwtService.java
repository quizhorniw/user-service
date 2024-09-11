package com.drevotiuk.service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Service class for handling JSON Web Token (JWT) operations, including token
 * generation, extraction, and validation.
 * Provides methods to generate JWTs, extract information from tokens, and
 * validate tokens based on the username and expiration.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
  @Value("${security.jwt.expiration}")
  private long tokenExpiration;

  private final KmsUtils kmsUtils;
  private final KeyManagementService keyService;

  /**
   * Generates a JWT token for the given username.
   *
   * @param username the username to include in the token.
   * @return the generated JWT token as a {@link String}.
   */
  public String generateToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    return Jwts.builder()
        .claims(claims)
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
        .signWith(getKey())
        .compact();
  }

  /**
   * Extracts the username from the given JWT token.
   *
   * @param token the JWT token from which to extract the username.
   * @return the username contained in the token as a {@link String}.
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Validates the given JWT token based on the username and expiration.
   *
   * @param token       the JWT token to validate.
   * @param userDetails the {@link UserDetails} object containing the username to
   *                    compare.
   * @return {@code true} if the token is valid and not expired; {@code false}
   *         otherwise.
   */
  public boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  /**
   * Retrieves the secret key used for signing and verifying JWT tokens.
   * 
   * @return the {@link SecretKey} used for JWT operations.
   */
  private SecretKey getKey() {
    byte[] encodedKeyBytes = kmsUtils.decrypt(keyService.getEncryptedSecretKey());
    byte[] keyBytes = Base64.getDecoder().decode(encodedKeyBytes);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Extracts a specific claim from the given JWT token using the provided
   * claim resolver function.
   * 
   * @param <T>           the type of the claim to extract.
   * @param token         the JWT token from which to extract the claim.
   * @param claimResolver a function that extracts the desired claim from the
   *                      {@link Claims} object.
   * @return the extracted claim value.
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
    final Claims claims = extractAllClaims(token);
    return claimResolver.apply(claims);
  }

  /**
   * Extracts all claims from the given JWT token.
   * 
   * @param token the JWT token from which to extract all claims.
   * @return the {@link Claims} object containing all claims from the token.
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Checks if the given JWT token is expired.
   * 
   * @param token the JWT token to check.
   * @return {@code true} if the token is expired; {@code false} otherwise.
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date of the given JWT token.
   * 
   * @param token the JWT token from which to extract the expiration date.
   * @return the expiration date of the token.
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }
}
