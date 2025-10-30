package hr.egraovac.alg.algebrabooking.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  private JwtUtil jwtUtil;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForJWTTokenGenerationThatNeedsToBeVeryLongForHS512Algorithm");
    ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

    userDetails = new User("testuser", "password",
        Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @Test
  void generateToken_shouldGenerateValidToken() {
    String token = jwtUtil.generateToken(userDetails);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void extractUsername_shouldReturnCorrectUsername() {
    String token = jwtUtil.generateToken(userDetails);

    String username = jwtUtil.extractUsername(token);

    assertEquals("testuser", username);
  }

  @Test
  void extractExpiration_shouldReturnFutureDate() {
    String token = jwtUtil.generateToken(userDetails);

    Date expirationDate = jwtUtil.extractExpiration(token);

    assertTrue(expirationDate.after(new Date()));
  }

  @Test
  void extractClaim_shouldExtractSubject() {
    String token = jwtUtil.generateToken(userDetails);

    String subject = jwtUtil.extractClaim(token, Claims::getSubject);

    assertEquals("testuser", subject);
  }

  @Test
  void validateToken_withValidToken_shouldReturnTrue() {
    String token = jwtUtil.generateToken(userDetails);

    Boolean isValid = jwtUtil.validateToken(token, userDetails);

    assertTrue(isValid);
  }

  @Test
  void validateToken_withDifferentUsername_shouldReturnFalse() {
    String token = jwtUtil.generateToken(userDetails);
    UserDetails differentUser = new User("differentuser", "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    Boolean isValid = jwtUtil.validateToken(token, differentUser);

    assertFalse(isValid);
  }


  @Test
  void extractUsername_withInvalidToken_shouldThrowException() {
    assertThrows(Exception.class, () -> {
      jwtUtil.extractUsername("invalid.token.here");
    });
  }

  @Test
  void validateToken_withNullToken_shouldThrowException() {
    assertThrows(Exception.class, () -> {
      jwtUtil.validateToken(null, userDetails);
    });
  }
}