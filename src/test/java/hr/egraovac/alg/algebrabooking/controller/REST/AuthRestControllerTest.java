package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.mapper.UserMapper;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @MockitoBean
  private JwtUtil jwtUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User guestUser;
  private UserDetails guestUserDetails;
  private String jwtToken;

  @BeforeEach
  void setUp() {
    // Guest user
    guestUser = new User();
    guestUser.setId(1L);
    guestUser.setUsername("guest");
    guestUser.setPassword(passwordEncoder.encode("password"));
    guestUser.setRoles(Set.of(UserRole.GUEST));

    guestUserDetails = UserMapper.toUserDetails(guestUser);

    jwtToken = jwtUtil.generateToken(UserMapper.toUserDetails(guestUser));


  }

  // ================== LOGIN TESTS ==================

  @Test
  void login_Success_ShouldReturnJwtAndUsername() throws Exception {
    when(userDetailsService.loadUserByUsername("guest")).thenReturn(guestUserDetails);
    when(jwtUtil.generateToken(guestUserDetails)).thenReturn(jwtToken);


    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "guest",
                  "password": "password"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(jwtToken));
  }

  @Test
  void login_InvalidCredentials_ShouldReturn401() throws Exception {
    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

    String requestJson = """
        {
          "username": "guest",
          "password": "wrongpass"
        }
        """;

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid username or password"));
  }

  // ================== TOKEN VALIDATION TESTS ==================

  @Test
  void validateToken_ValidToken_ShouldReturnOk() throws Exception {
    when(jwtUtil.extractUsername("valid.jwt")).thenReturn("guest");
    when(userDetailsService.loadUserByUsername("guest")).thenReturn(guestUserDetails);
    when(jwtUtil.validateToken("valid.jwt", guestUserDetails)).thenReturn(true);

    mockMvc.perform(get("/api/auth/validate")
            .header("Authorization", "Bearer valid.jwt"))
        .andExpect(status().isOk())
        .andExpect(content().string("Token is valid"));
  }

  @Test
  void validateToken_InvalidToken_ShouldReturn401() throws Exception {
    when(jwtUtil.extractUsername("invalid.jwt")).thenReturn("guest");
    when(userDetailsService.loadUserByUsername("guest")).thenReturn(guestUserDetails);
    when(jwtUtil.validateToken("invalid.jwt", guestUserDetails)).thenReturn(false);

    mockMvc.perform(get("/api/auth/validate")
            .header("Authorization", "Bearer invalid.jwt"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid token"));
  }

  @Test
  void validateToken_MalformedHeader_ShouldReturn401() throws Exception {
    mockMvc.perform(get("/api/auth/validate")
            .header("Authorization", "invalidheader"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Invalid token"));
  }

  @Test
  void validateToken_ExceptionDuringValidation_ShouldReturn401() throws Exception {
    when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Error"));

    mockMvc.perform(get("/api/auth/validate")
            .header("Authorization", "Bearer corrupted.jwt"))
        .andExpect(status().isUnauthorized());
  }
}
