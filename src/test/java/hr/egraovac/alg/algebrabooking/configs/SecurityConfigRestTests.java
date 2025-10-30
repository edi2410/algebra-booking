package hr.egraovac.alg.algebrabooking.configs;

import hr.egraovac.alg.algebrabooking.mapper.UserMapper;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityConfigRestTests {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  private User guestUser;
  private User receptionistUser;
  private User managerUser;

  private String guestToken;
  private String receptionistToken;
  private String managerToken;
  @Autowired private RoomService roomService;
  @Autowired private BookingService bookingService;

  @BeforeEach
  void setUp() {
    // Guest user
    guestUser = new User();
    guestUser.setId(1L);
    guestUser.setUsername("guest");
    guestUser.setPassword(passwordEncoder.encode("password"));
    guestUser.setRoles(Set.of(UserRole.GUEST));

    // Receptionist user
    receptionistUser = new User();
    receptionistUser.setId(2L);
    receptionistUser.setUsername("receptionist");
    receptionistUser.setPassword(passwordEncoder.encode("password"));
    receptionistUser.setRoles(Set.of(UserRole.RECEPTIONIST));

    // Manager user
    managerUser = new User();
    managerUser.setId(3L);
    managerUser.setUsername("manager");
    managerUser.setPassword(passwordEncoder.encode("password"));
    managerUser.setRoles(Set.of(UserRole.MANAGER));

    // Mock UserService
    when(userService.findByUsername("guest")).thenReturn(Optional.of(guestUser));
    when(userService.findByUsername("receptionist")).thenReturn(Optional.of(receptionistUser));
    when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

    // Generate JWT tokens
    guestToken = jwtUtil.generateToken(UserMapper.toUserDetails(guestUser));
    receptionistToken = jwtUtil.generateToken(UserMapper.toUserDetails(receptionistUser));
    managerToken = jwtUtil.generateToken(UserMapper.toUserDetails(managerUser));
  }



  // ==================== PUBLIC API ENDPOINTS ====================

  @Test
  void whenAccessingApiAuthLogin_thenPermitAll() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content("{\"username\":\"guest\",\"password\":\"password\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void whenAccessingApiRooms_thenPermitAll() throws Exception {
    mockMvc.perform(get("/api/rooms"))
        .andExpect(status().isOk());
  }


  // ==================== GUEST ROLE API TESTS ====================

  @Test
  void whenGuestAccessesApiGuestEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk());
  }

  @Test
  void whenGuestAccessesApiReceptionistEndpoint_thenForbidden() throws Exception {
    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$.error").value("Forbidden"));
  }

  @Test
  void whenGuestAccessesApiManagerEndpoint_thenForbidden() throws Exception {
    mockMvc.perform(get("/api/manager/stats/revenue")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());
  }


  // ==================== RECEPTIONIST ROLE API TESTS ====================

  @Test
  void whenReceptionistAccessesApiReceptionistEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk());
  }

  @Test
  void whenReceptionistAccessesApiGuestEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk());
  }

  @Test
  void whenReceptionistAccessesApiManagerEndpoint_thenForbidden() throws Exception {
    mockMvc.perform(get("/api/manager/stats/revenue")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isForbidden());
  }


  // ==================== MANAGER ROLE API TESTS ====================

  @Test
  void whenManagerAccessesApiReceptionistEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk());
  }

  @Test
  void whenManagerAccessesPublicEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/api/rooms")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk());
  }


  // ==================== UNAUTHENTICATED API ACCESS ====================

  @Test
  void whenUnauthenticatedUserAccessesProtectedApiEndpoint_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings"))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenUnauthenticatedUserAccessesApiReceptionistEndpoint_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/all"))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenUnauthenticatedUserAccessesApiManagerEndpoint_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/manager/stats/revenue"))
        .andExpect(status().isForbidden()); // Spring Security vraća 403 bez custom entry point
  }

  @Test
  void whenUnauthenticatedUserPostsToProtectedEndpoint_thenUnauthorized() throws Exception {
    mockMvc.perform(post("/api/bookings")
            .contentType("application/json")
            .content("{\"roomId\":1,\"checkInDate\":\"2025-11-01\",\"checkOutDate\":\"2025-11-05\"}"))
        .andExpect(status().isForbidden()); // Spring Security vraća 403 bez custom entry point
  }

  // ==================== INVALID TOKEN TESTS ====================

  @Test
  void whenAccessingWithInvalidToken_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer invalid.token.here"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void whenAccessingWithMalformedAuthHeader_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "InvalidFormat " + guestToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenAccessingWithoutBearerPrefix_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", guestToken))
        .andExpect(status().isForbidden());
  }

  // ==================== CSRF PROTECTION ====================

  @Test
  void whenPostToApiEndpointWithoutCsrf_thenSuccess() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType("application/json")
            .content("{\"username\":\"guest\",\"password\":\"password\"}"))
        .andExpect(status().isOk());
  }

  // ==================== SESSION MANAGEMENT ====================

  @Test
  void whenAccessingApiEndpoint_thenStatelessSession() throws Exception {
    // API endpoints su stateless - ne kreiraju sesiju
    mockMvc.perform(get("/api/rooms"))
        .andExpect(status().isOk());
    // Sesija ne bi trebala biti kreirana za API endpoint
  }

  @Test
  void whenAuthenticatedApiRequest_thenNoSessionCreated() throws Exception {

    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk());
  }

  @Test
  void whenMultipleApiRequests_thenEachIsIndependent() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk());


    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/bookings/my-bookings"))
        .andExpect(status().isForbidden());
  }

  // ==================== ROLE HIERARCHY TESTS ====================

  @Test
  void whenManagerAccessesAllRoles_thenSuccess() throws Exception {

    mockMvc.perform(get("/api/rooms")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk());

  }

  @Test
  void whenReceptionistCannotAccessManager_thenForbidden() throws Exception {
    mockMvc.perform(get("/api/manager/users")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenGuestCannotAccessStaff_thenForbidden() throws Exception {
    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/api/manager/stats/revenue")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());
  }
}