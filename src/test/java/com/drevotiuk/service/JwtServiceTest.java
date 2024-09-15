package com.drevotiuk.service;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
  @Mock
  private KmsUtils kmsUtils;
  @Mock
  private KeyManagementService keyManagementService;
  private JwtService underTest;

  @BeforeEach
  void setUp() {
    underTest = new JwtService(kmsUtils, keyManagementService);
  }

  @Test
  void shouldGenerateJwt() throws NoSuchFieldException, IllegalAccessException {
    // given
    setDeclaredField(underTest, "tokenExpiration", 999999);
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("some-really-strong-decrypted-key".getBytes()));

    // when
    String jwt = underTest.generateToken("test username");

    // then
    Claims claims = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .build()
        .parseSignedClaims(jwt)
        .getPayload();

    assertThat(claims.getSubject()).isEqualTo("test username");
    assertThat(claims.getIssuedAt()).isBefore(Instant.now());
    assertThat(claims.getExpiration()).isEqualToIgnoringSeconds(claims.getIssuedAt().toInstant().plusMillis(999999));
  }

  @Test
  void shouldExtractUsername() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 999999))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("some-really-strong-decrypted-key".getBytes()));

    // when
    String username = underTest.extractUsername(jwt);

    // then
    assertThat(username).isEqualTo("test username");
  }

  @Test
  void shouldThrow_whenSecretKeyIsInvalidInUsernameExtraction() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 999999))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("INVALID very strong 256bit secret key".getBytes()));

    // when
    // then
    assertThatThrownBy(() -> underTest.extractUsername(jwt))
        .isInstanceOf(SignatureException.class)
        .hasMessageContaining("JWT signature");
  }

  @Test
  void shouldValidateJwt() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 999999))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("some-really-strong-decrypted-key".getBytes()));
    UserDetails mockUserDetails = mock(UserDetails.class);
    given(mockUserDetails.getUsername()).willReturn("test username");

    // when
    boolean validated = underTest.validateToken(jwt, mockUserDetails);

    // then
    assertThat(validated).isTrue();
  }

  @Test
  void shouldNotValidate_whenUsernameDoesNotMatch() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 999999))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("some-really-strong-decrypted-key".getBytes()));
    UserDetails mockUserDetails = mock(UserDetails.class);
    given(mockUserDetails.getUsername()).willReturn("some other usename");

    // when
    boolean validated = underTest.validateToken(jwt, mockUserDetails);

    // then
    assertThat(validated).isFalse();
  }

  @Test
  void shouldThrow_whenJwtIsExpired() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() - 100))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("some-really-strong-decrypted-key".getBytes()));
    UserDetails mockUserDetails = mock(UserDetails.class);

    // when
    // then
    assertThatThrownBy(() -> underTest.validateToken(jwt, mockUserDetails))
        .isInstanceOf(ExpiredJwtException.class)
        .hasMessageContaining("JWT expired");
  }

  @Test
  void shouldThrow_whenSecretKeyIsInvalidInJwtValidation() {
    // given
    Map<String, Object> claims = new HashMap<>();
    String jwt = Jwts.builder()
        .claims(claims)
        .subject("test username")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 999999))
        .signWith(Keys.hmacShaKeyFor("some-really-strong-decrypted-key".getBytes()))
        .compact();
    given(keyManagementService.getEncryptedSecretKey()).willReturn("some-encrypted-key".getBytes());
    given(kmsUtils.decrypt("some-encrypted-key".getBytes()))
        .willReturn(Base64.getEncoder().encode("INVALID very strong 256bit secret key".getBytes()));
    UserDetails mockUserDetails = mock(UserDetails.class);

    // when
    // then
    assertThatThrownBy(() -> underTest.validateToken(jwt, mockUserDetails))
        .isInstanceOf(SignatureException.class)
        .hasMessageContaining("JWT signature");
  }

  private void setDeclaredField(Object target, String fieldName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
    field.setAccessible(false);
  }
}
