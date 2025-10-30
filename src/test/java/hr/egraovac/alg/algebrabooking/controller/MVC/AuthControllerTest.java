package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.configs.TestSecurityConfig;
import hr.egraovac.alg.algebrabooking.filter.JwtAuthenticationFilter;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private JwtUtil jwtUtil;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("john");
    testUser.setEmail("john@example.com");
  }

  // ==================== LOGIN PAGE ====================

  @Test
  void whenAccessingLogin_thenReturnsLoginView() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/login"));
  }

  // ==================== REGISTER PAGE ====================

  @Test
  void whenAccessingRegister_thenReturnsRegisterView() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/register"));
  }

  // ==================== SUCCESSFUL REGISTRATION ====================

  @Test
  void whenRegisteringNewUser_thenRedirectsToLogin() throws Exception {
    when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

    mockMvc.perform(post("/register")
            .with(csrf())
            .param("username", "newuser")
            .param("email", "new@example.com")
            .param("password", "secret")
            .param("fullName", "New User"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?registered"));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userService).saveUser(userCaptor.capture());
    User saved = userCaptor.getValue();

    assertThat(saved.getUsername()).isEqualTo("newuser");
    assertThat(saved.getEmail()).isEqualTo("new@example.com");
    assertThat(saved.getFullName()).isEqualTo("New User");
    assertThat(saved.getPassword()).isEqualTo("encoded-secret");
    assertThat(saved.getRoles()).contains(UserRole.GUEST);
  }
  // ==================== DUPLICATE USERNAME ====================

  @Test
  void whenRegisteringExistingUsername_thenReturnsRegisterViewWithError() throws Exception {
    when(userService.findByUsername("existing")).thenReturn(Optional.of(testUser));

    mockMvc.perform(post("/register")
            .with(csrf())
            .param("username", "existing")
            .param("email", "exists@example.com")
            .param("password", "secret"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/register"))
        .andExpect(model().attributeExists("error"))
        .andExpect(model().attribute("error", "Username already exists"));

    verify(userService, never()).saveUser(any(User.class));
  }

  // ==================== MISSING CSRF TOKEN ====================

  @Test
  void whenRegisterWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/register")
            .param("username", "user")
            .param("email", "user@example.com")
            .param("password", "1234"))
        .andExpect(status().isForbidden());
  }
}
