package com.drevotiuk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.drevotiuk.filter.JwtFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuration class for setting up Spring Security in the application.
 * This class configures security settings, including HTTP security,
 * authentication,
 * and authorization rules. It also sets up the JWT filter and defines the
 * authentication provider and manager.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final BCryptPasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final JwtFilter jwtFilter;

  /**
   * Configures HTTP security settings, including disabling CSRF protection,
   * defining access rules for specific endpoints, and configuring session
   * management.
   * Adds the JWT filter to the security filter chain before the username and
   * password
   * authentication filter.
   *
   * @param http the {@link HttpSecurity} instance used to configure security
   *             settings.
   * @return a {@link SecurityFilterChain} instance.
   * @throws Exception if an error occurs during configuration.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/v*/users/login", "/api/v*/users/register",
            "/api/v*/users/confirm**")
        .permitAll()
        .antMatchers("/api/v*/management/users/**")
        .hasRole("ADMIN")
        .anyRequest().authenticated().and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Provides an {@link AuthenticationProvider} bean that uses a
   * {@link DaoAuthenticationProvider}.
   * Configures the provider with the {@link BCryptPasswordEncoder} for password
   * encoding
   * and the {@link UserDetailsService} for user details retrieval.
   *
   * @return an {@link AuthenticationProvider} instance.
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder);
    provider.setUserDetailsService(userDetailsService);
    return provider;
  }

  /**
   * Provides an {@link AuthenticationManager} bean based on the provided
   * {@link AuthenticationConfiguration}.
   * This manager is used for performing authentication operations.
   *
   * @param config the {@link AuthenticationConfiguration} used to create the
   *               manager.
   * @return an {@link AuthenticationManager} instance.
   * @throws Exception if an error occurs during creation.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
