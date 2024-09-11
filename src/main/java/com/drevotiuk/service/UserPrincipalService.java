package com.drevotiuk.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.drevotiuk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom implementation of the {@link UserDetailsService} interface for loading
 * user-specific data.
 * This service is used by Spring Security to retrieve user details based on the
 * provided username (email).
 * It interacts with the {@link UserRepository} to fetch user information from
 * the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPrincipalService implements UserDetailsService {
  private final UserRepository userRepository;

  /**
   * Loads a user by their username (email). This method is used by Spring
   * Security during the authentication process to retrieve user details.
   * 
   * @param email the email of the user to be retrieved.
   * @return a {@link UserDetails} object containing user information.
   * @throws UsernameNotFoundException if no user with the provided email is
   *                                   found.
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email).orElseThrow(() -> {
      log.warn("User with email {} not found", email);
      return new UsernameNotFoundException(String.format("User with email %s not found", email));
    });
  }
}
