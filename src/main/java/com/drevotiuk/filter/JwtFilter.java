package com.drevotiuk.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.drevotiuk.service.UserPrincipalService;
import com.drevotiuk.service.JwtService;

import io.jsonwebtoken.JwtException;

/**
 * A filter that processes JWT authentication tokens for incoming HTTP requests.
 * <p>
 * This filter extracts the JWT token from the `Authorization` header, validates
 * it,
 * and sets the authentication context if the token is valid. It handles
 * exceptions
 * that occur during the authentication process using the provided
 * {@link HandlerExceptionResolver}.
 * </p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserPrincipalService principalService;
  private final HandlerExceptionResolver exceptionResolver;

  public JwtFilter(JwtService jwtService, UserPrincipalService principalService,
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.jwtService = jwtService;
    this.principalService = principalService;
    this.exceptionResolver = exceptionResolver;
  }

  /**
   * Performs the filter logic for JWT authentication.
   * <p>
   * This method checks if the request has a valid `Authorization` header,
   * extracts and validates
   * the JWT token, and sets the authentication context if the token is valid. If
   * the token is invalid
   * or if an exception occurs, the filter chain proceeds without setting the
   * authentication.
   * </p>
   *
   * @param request     the HTTP request
   * @param response    the HTTP response
   * @param filterChain the filter chain to pass control to the next filter
   * @throws ServletException if a servlet error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (!isValidAuthHeader(authHeader)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = extractToken(authHeader);
      String email = jwtService.extractUsername(token);
      if (email == null || isAuthenticationPresent()) {
        filterChain.doFilter(request, response);
        return;
      }

      validateToken(token, email);
      authenticateUser(request, email);
      filterChain.doFilter(request, response);
    } catch (JwtException | UsernameNotFoundException e) {
      handleException(request, response, e);
    }
  }

  /**
   * Checks if the `Authorization` header is valid.
   *
   * @param authHeader the `Authorization` header value
   * @return {@code true} if the header is valid and starts with "Bearer ",
   *         otherwise {@code false}
   */
  private boolean isValidAuthHeader(String authHeader) {
    return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
  }

  /**
   * Extracts the token from the `Authorization` header.
   *
   * @param authHeader the `Authorization` header value
   * @return the extracted token
   */
  private String extractToken(String authHeader) {
    return authHeader.substring(BEARER_PREFIX.length());
  }

  /**
   * Checks if the authentication is already present in the security context.
   *
   * @return {@code true} if authentication is present, otherwise {@code false}
   */
  private boolean isAuthenticationPresent() {
    return SecurityContextHolder.getContext().getAuthentication() != null;
  }

  /**
   * Validates the JWT token using the provided email.
   *
   * @param token the JWT token
   * @param email the email extracted from the token
   * @return {@code true} if the token is valid, otherwise {@code false}
   */
  private void validateToken(String token, String email) {
    UserDetails userDetails = principalService.loadUserByUsername(email);
    if (!jwtService.validateToken(token, userDetails))
      throw new JwtException("JWT is invalid"); // For case when JwtService won't throw exception itself
  }

  /**
   * Authenticates the user and sets the authentication context.
   *
   * @param request the HTTP request
   * @param email   the email extracted from the JWT token
   */
  private void authenticateUser(HttpServletRequest request, String email) {
    UserDetails userDetails = principalService.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  /**
   * Handles exceptions that occur during JWT authentication.
   *
   * @param request   the HTTP request
   * @param response  the HTTP response
   * @param exception the exception that occurred
   */
  private void handleException(HttpServletRequest request, HttpServletResponse response, Exception exception) {
    exceptionResolver.resolveException(request, response, null, exception);
  }
}
