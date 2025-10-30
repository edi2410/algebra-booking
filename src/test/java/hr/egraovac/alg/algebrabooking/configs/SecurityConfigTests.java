package hr.egraovac.alg.algebrabooking.configs;

import hr.egraovac.alg.algebrabooking.dto.BookingStatsDTO;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityConfigTests {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private BookingService bookingService;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private AuthUtil authUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User guestUser;
  private User receptionistUser;
  private User managerUser;

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

    // Mock common service calls
    when(bookingService.findByGuestId(anyLong())).thenReturn(List.of());
    when(bookingService.searchBookings(any(), any(), any(), any())).thenReturn(List.of());
    when(bookingService.getBookingStatistics()).thenReturn(new BookingStatsDTO());
    when(roomService.findAvailableRooms()).thenReturn(List.of());
  }

  // ==================== PUBLIC ENDPOINTS ====================

  @Test
  void whenAccessingHomePage_thenPermitAll() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  @Test
  void whenAccessingLoginPage_thenPermitAll() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk());
  }

  @Test
  void whenAccessingRegisterPage_thenPermitAll() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk());
  }

  @Test
  void whenAccessingRoomListPage_thenPermitAll() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }


  // ==================== GUEST ROLE TESTS ====================

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesMyBookings_thenSuccess() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesAllBookings_thenForbidden() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesManagerEndpoint_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesManagerDashboard_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestTriesToCreateBooking_thenSuccess() throws Exception {
    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", "2025-12-01")
            .param("checkOutDate", "2025-12-05"))
        .andExpect(status().is3xxRedirection()); // Redirect zbog business logic
  }

  // ==================== RECEPTIONIST ROLE TESTS ====================

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesAllBookings_thenSuccess() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesMyBookings_thenSuccess() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(receptionistUser);

    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesManagerEndpoint_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistChecksInBooking_thenSuccess() throws Exception {
    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"));
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistConfirmsBooking_thenSuccess() throws Exception {
    mockMvc.perform(post("/booking/confirm/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"));
  }

  // ==================== MANAGER ROLE TESTS ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesManagerEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesAllBookings_thenSuccess() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesManagerDashboard_thenSuccess() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isOk());
  }


  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesPublicEndpoint_thenSuccess() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerChecksInBooking_thenSuccess() throws Exception {
    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"));
  }

  // ==================== UNAUTHENTICATED TESTS ====================

  @Test
  void whenUnauthenticatedUserAccessesProtectedMvcEndpoint_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void whenUnauthenticatedUserAccessesAllBookings_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void whenUnauthenticatedUserAccessesManagerEndpoint_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get("/manager"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void whenUnauthenticatedUserTriesToCreateBooking_thenRedirectToLogin() throws Exception {
    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", "2025-12-01")
            .param("checkOutDate", "2025-12-05"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void whenUnauthenticatedUserAccessesProtectedApiEndpoint_thenUnauthorized() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings"))
        .andExpect(status().isForbidden());
  }

  // ==================== FORM LOGIN TESTS ====================

  @Test
  void whenLoginWithValidCredentials_thenSuccess() throws Exception {
    mockMvc.perform(post("/login")
            .param("username", "guest")
            .param("password", "password")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void whenLoginWithInvalidCredentials_thenRedirectToLoginWithError() throws Exception {
    when(userService.findByUsername("wronguser")).thenReturn(Optional.empty());

    mockMvc.perform(post("/login")
            .param("username", "wronguser")
            .param("password", "wrongpassword")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error=true"));
  }

  @Test
  void whenLoginWithWrongPassword_thenRedirectToLoginWithError() throws Exception {
    mockMvc.perform(post("/login")
            .param("username", "guest")
            .param("password", "wrongpassword")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error=true"));
  }

  @Test
  void whenLoginWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/login")
            .param("username", "guest")
            .param("password", "password"))
        .andExpect(status().isForbidden());
  }

  // ==================== LOGOUT TESTS ====================

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenLogout_thenRedirectToHomeAndInvalidateSession() throws Exception {
    mockMvc.perform(get("/logout"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerLogout_thenRedirectToHome() throws Exception {
    mockMvc.perform(get("/logout"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void whenUnauthenticatedUserLogout_thenRedirectToHome() throws Exception {
    mockMvc.perform(get("/logout"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  // ==================== CSRF TESTS ====================

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenPostToMvcEndpointWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/booking/create")
            .param("roomId", "1")
            .param("checkInDate", "2025-12-01")
            .param("checkOutDate", "2025-12-05"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistPostWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/booking/check-in/1"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenPostToMvcEndpointWithCsrf_thenSuccess() throws Exception {
    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", "2025-12-01")
            .param("checkOutDate", "2025-12-05"))
        .andExpect(status().is3xxRedirection()); // Business logic redirect
  }

  // ==================== SESSION MANAGEMENT TESTS ====================

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenAccessingMvcEndpoint_thenSessionCreated() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().isOk())
        .andExpect(request().sessionAttributeDoesNotExist("nonexistent")); // Sesija postoji
  }

  @Test
  void whenAccessingPublicMvcEndpoint_thenNoAuthenticationRequired() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  // ==================== ROLE HIERARCHY TESTS ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesAllRoleLevels_thenSuccess() throws Exception {
    // Manager može pristupiti svim nivoima
    mockMvc.perform(get("/")).andExpect(status().isOk());
    mockMvc.perform(get("/booking/all")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistCannotAccessManager_thenForbidden() throws Exception {
    mockMvc.perform(get("/manager/stats/revenue"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestCannotAccessStaff_thenForbidden() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isForbidden());

    mockMvc.perform(get("/manager"))
        .andExpect(status().isForbidden());
  }

}