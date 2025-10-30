package hr.egraovac.alg.algebrabooking.utils;

import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUtilsTest {

  @Mock
  private UserService userService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private AuthUtil authUtil;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void isAuthenticated_whenAuthenticationIsNull_shouldReturnFalse() {
    when(securityContext.getAuthentication()).thenReturn(null);

    boolean result = authUtil.isAuthenticated();

    assertFalse(result);
    verify(securityContext).getAuthentication();
  }

  @Test
  void isAuthenticated_whenAuthenticationIsNotAuthenticated_shouldReturnFalse() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);


    boolean result = authUtil.isAuthenticated();

    assertFalse(result);
    verify(authentication).isAuthenticated();
  }

  @Test
  void isAuthenticated_whenAuthenticationIsAnonymous_shouldReturnFalse() {
    AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
        "key",
        "anonymous",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
    );
    when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

    boolean result = authUtil.isAuthenticated();


    assertFalse(result);
  }

  @Test
  void isAuthenticated_whenAuthenticationIsValid_shouldReturnTrue() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);


    boolean result = authUtil.isAuthenticated();


    assertTrue(result);
    verify(authentication).isAuthenticated();
  }

  @Test
  void extractAuthorizedUser_whenUserExists_shouldReturnUser() {
    String username = "testuser";
    User expectedUser = new User();
    expectedUser.setUsername(username);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
    when(userService.findByUsername(username)).thenReturn(Optional.of(expectedUser));

    User result = authUtil.extractAuthorizedUser();

    assertNotNull(result);
    assertEquals(expectedUser, result);
    assertEquals(username, result.getUsername());
    verify(authentication).getName();
    verify(userService).findByUsername(username);
  }

  @Test
  void extractAuthorizedUser_whenUserDoesNotExist_shouldThrowException() {
    String username = "nonexistent";

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
    when(userService.findByUsername(username)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      authUtil.extractAuthorizedUser();
    });

    assertEquals("User not found", exception.getMessage());
    verify(authentication).getName();
    verify(userService).findByUsername(username);
  }

  @Test
  void extractAuthorizedUser_whenAuthenticationIsNull_shouldThrowNullPointerException() {
    when(securityContext.getAuthentication()).thenReturn(null);

    assertThrows(NullPointerException.class, () -> {
      authUtil.extractAuthorizedUser();
    });
  }

  @Test
  void extractUsernameFromAuthorizedUser_whenAuthenticationExists_shouldReturnUsername() {
    String expectedUsername = "testuser";

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(expectedUsername);

    String result = authUtil.extractUsernameFromAuthorizedUser();

    assertEquals(expectedUsername, result);
    verify(authentication).getName();
  }

  @Test
  void extractUsernameFromAuthorizedUser_whenAuthenticationIsNull_shouldThrowNullPointerException() {
    // Arrange
    when(securityContext.getAuthentication()).thenReturn(null);

    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
      authUtil.extractUsernameFromAuthorizedUser();
    });
  }

  @Test
  void extractUsernameFromAuthorizedUser_withRealAuthentication_shouldReturnUsername() {
    String username = "realuser";
    Authentication realAuth = new UsernamePasswordAuthenticationToken(
        username,
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContext realContext = SecurityContextHolder.createEmptyContext();
    realContext.setAuthentication(realAuth);
    SecurityContextHolder.setContext(realContext);

    String result = authUtil.extractUsernameFromAuthorizedUser();


    assertEquals(username, result);
  }
}