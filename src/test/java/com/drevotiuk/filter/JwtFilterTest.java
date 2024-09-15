package com.drevotiuk.filter;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.drevotiuk.model.UserPrincipal;
import com.drevotiuk.model.UserRole;
import com.drevotiuk.service.JwtService;
import com.drevotiuk.service.UserPrincipalService;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {
  @Mock
  private JwtService jwtService;
  @Mock
  private UserPrincipalService userPrincipalService;
  @Mock
  private HandlerExceptionResolver handlerExceptionResolver;
  @Mock
  private FilterChain filterChain;
  @Mock
  private HttpServletRequest httpServletRequest;
  @Mock
  private HttpServletResponse httpServletResponse;
  @Mock
  private SecurityContext securityContext;
  private JwtFilter underTest;

  @BeforeEach
  void setUp() {
    underTest = new JwtFilter(jwtService, userPrincipalService, handlerExceptionResolver);
  }

  @Test
  void shouldFilterRequestAndAuthenticateWithJwt() throws IOException, ServletException {
    // given
    given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer test-jwt");
    given(jwtService.extractUsername("test-jwt")).willReturn("johndoe@mail.com");
    SecurityContextHolder.setContext(securityContext);
    // Authentication mockAuthentication = mock(Authentication.class);
    given(securityContext.getAuthentication()).willReturn(null);

    UserPrincipal user = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        true);
    given(userPrincipalService.loadUserByUsername(user.getEmail())).willReturn(user);
    given(jwtService.validateToken("test-jwt", user)).willReturn(true);

    // when
    underTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(handlerExceptionResolver, never()).resolveException(
        eq(httpServletRequest),
        eq(httpServletResponse),
        eq(null),
        any(Exception.class));

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        user, null, user.getAuthorities());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

    ArgumentCaptor<UsernamePasswordAuthenticationToken> authTokenArgumentCaptor = ArgumentCaptor
        .forClass(UsernamePasswordAuthenticationToken.class);
    verify(securityContext).setAuthentication(authTokenArgumentCaptor.capture());

    UsernamePasswordAuthenticationToken capturedAuthToken = authTokenArgumentCaptor.getValue();
    assertThat(capturedAuthToken).isEqualTo(authToken);
  }

  @Test
  void shouldNotAuthenticate_whenJwtInvalid() throws IOException, ServletException {
    // given
    given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer test-jwt");
    given(jwtService.extractUsername("test-jwt")).willReturn("johndoe@mail.com");
    SecurityContextHolder.setContext(securityContext);
    given(securityContext.getAuthentication()).willReturn(null);

    UserPrincipal user = new UserPrincipal(
        ObjectId.get(),
        "John",
        "Doe",
        LocalDate.now(),
        "johndoe@mail.com",
        "qwerty123",
        UserRole.USER,
        false,
        true);
    given(userPrincipalService.loadUserByUsername(user.getEmail())).willReturn(user);
    given(jwtService.validateToken("test-jwt", user)).willThrow(JwtException.class); // JWT is invalid

    // when
    underTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(handlerExceptionResolver).resolveException(
        eq(httpServletRequest),
        eq(httpServletResponse),
        eq(null),
        any(JwtException.class));

    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  void shouldNotAuthenticate_whenUserNotFoundInDatabase() throws IOException, ServletException {
    // given
    given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer test-jwt");
    given(jwtService.extractUsername("test-jwt")).willReturn("johndoe@mail.com");
    SecurityContextHolder.setContext(securityContext);
    given(securityContext.getAuthentication()).willReturn(null);

    given(userPrincipalService.loadUserByUsername("johndoe@mail.com"))
        .willThrow(new UsernameNotFoundException("TEST username not found"));

    // when
    underTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(handlerExceptionResolver).resolveException(
        eq(httpServletRequest),
        eq(httpServletResponse),
        eq(null),
        any(UsernameNotFoundException.class));

    verify(jwtService, never()).validateToken(eq("test-jwt"), any());
    verify(securityContext, never()).setAuthentication(any());
  }

  @Test
  void shouldNotAuthenticate_whenAlreadyAuthenticated() throws IOException, ServletException {
    // given
    given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer test-jwt");
    given(jwtService.extractUsername("test-jwt")).willReturn("johndoe@mail.com");
    SecurityContextHolder.setContext(securityContext);
    Authentication mockAuthentication = mock(Authentication.class);
    given(securityContext.getAuthentication()).willReturn(mockAuthentication);

    // when
    underTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(userPrincipalService, never()).loadUserByUsername("johndoe@mail.com");
    verify(jwtService, never()).validateToken(eq("test-jwt"), any());
    verify(securityContext, never()).setAuthentication(any());
    verify(handlerExceptionResolver, never()).resolveException(
        eq(httpServletRequest),
        eq(httpServletResponse),
        eq(null),
        any(Exception.class));
  }

  @Test
  void shouldNotAuthenticate_whenAuthHeaderInvalid() throws IOException, ServletException {
    // given
    given(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("invalid auth header");

    // when
    underTest.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

    // then
    verify(userPrincipalService, never()).loadUserByUsername(anyString());
    verify(jwtService, never()).extractUsername("invalid auth header");
    verify(jwtService, never()).validateToken(anyString(), any());
    verify(securityContext, never()).setAuthentication(any());
    verify(handlerExceptionResolver, never()).resolveException(
        eq(httpServletRequest),
        eq(httpServletResponse),
        eq(null),
        any(Exception.class));
  }
}
