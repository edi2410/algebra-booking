package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.configs.TestSecurityConfig;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class HomeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private AuthUtil authUtil;

  @MockitoBean
  private JwtUtil jwtUtil;

  @MockitoBean
  private UserService userService;

  private Room testRoom;
  private User managerUser;
  private User guestUser;

  @BeforeEach
  void setUp() {
    testRoom = new Room();
    testRoom.setId(1L);
    testRoom.setRoomNumber("101");
    testRoom.setRoomType(RoomType.SINGLE);
    testRoom.setStatus(RoomStatus.AVAILABLE);
    testRoom.setPricePerNight(BigDecimal.valueOf(100.00));
    testRoom.setCapacity(2);

    managerUser = new User();
    managerUser.setUsername("manager");
    managerUser.addRole(UserRole.MANAGER);

    guestUser = new User();
    guestUser.setUsername("guest");
    guestUser.addRole(UserRole.GUEST);
  }

  @Test
  void whenUnauthenticatedUserVisitsHomeWithoutFilters_thenShowsAvailableRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.findAvailableRooms()).thenReturn(List.of(testRoom));

    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"))
        .andExpect(model().attributeExists("rooms"))
        .andExpect(model().attributeExists("roomTypes"));

    verify(roomService).findAvailableRooms();
  }

  @Test
  void whenUnauthenticatedUserUsesFilters_thenSearchRoomsCalledWithAvailableStatus() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "DOUBLE")
            .param("maxPrice", "150.00"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), eq(RoomType.DOUBLE),
        eq(new BigDecimal("150.00")), isNull(), isNull(), isNull());
  }

  @Test
  void whenManagerAuthenticatedWithoutFilters_thenShowsAllRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(managerUser);
    when(roomService.allRooms()).thenReturn(List.of(testRoom));

    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));

    verify(roomService).allRooms();
  }

  @Test
  void whenManagerAuthenticatedWithFilters_thenSearchRoomsWithoutStatus() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(managerUser);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "SUITE")
            .param("maxPrice", "200.00"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(isNull(), eq(RoomType.SUITE),
        eq(new BigDecimal("200.00")), isNull(), isNull(), isNull());
  }

  @Test
  void whenGuestAuthenticatedWithoutFilters_thenShowsAvailableRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findAvailableRooms()).thenReturn(List.of(testRoom));

    mockMvc.perform(get("/"))
        .andExpect(status().isOk());

    verify(roomService).findAvailableRooms();
  }

  @Test
  void whenGuestAuthenticatedWithFilters_thenSearchRoomsWithAvailableStatus() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "SINGLE")
            .param("checkIn", "2025-12-01"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), eq(RoomType.SINGLE),
        isNull(), isNull(), eq(LocalDate.of(2025, 12, 1)), isNull());
  }

  @Test
  void whenUnauthenticatedWithOnlyMaxPrice_thenCallsSearchRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("maxPrice", "100.00"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), isNull(),
        eq(new BigDecimal("100.00")), isNull(), isNull(), isNull());
  }

  @Test
  void whenUnauthenticatedWithOnlyMinCapacity_thenCallsSearchRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("minCapacity", "2"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), isNull(), isNull(),
        eq(2), isNull(), isNull());
  }

  @Test
  void whenUnauthenticatedWithOnlyCheckIn_thenCallsSearchRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("checkIn", "2025-11-01"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), isNull(), isNull(),
        isNull(), eq(LocalDate.of(2025, 11, 1)), isNull());
  }

  @Test
  void whenUnauthenticatedWithOnlyCheckOut_thenCallsSearchRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("checkOut", "2025-11-10"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), isNull(), isNull(),
        isNull(), isNull(), eq(LocalDate.of(2025, 11, 10)));
  }

  @Test
  void whenManagerWithOnlyRoomType_thenCallsSearchRooms() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(managerUser);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "DOUBLE"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(isNull(), eq(RoomType.DOUBLE), isNull(),
        isNull(), isNull(), isNull());
  }

  @Test
  void whenGuestWithAllFilters_thenCallsSearchRoomsWithAllParams() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(true);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "SUITE")
            .param("maxPrice", "250.00")
            .param("minCapacity", "3")
            .param("checkIn", "2025-12-15")
            .param("checkOut", "2025-12-20"))
        .andExpect(status().isOk());

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE), eq(RoomType.SUITE),
        eq(new BigDecimal("250.00")), eq(3),
        eq(LocalDate.of(2025, 12, 15)), eq(LocalDate.of(2025, 12, 20)));
  }

  @Test
  void whenModelAttributesSet_thenAllAttributesPresent() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(testRoom));

    mockMvc.perform(get("/")
            .param("roomType", "SINGLE")
            .param("maxPrice", "120.00")
            .param("minCapacity", "1")
            .param("checkIn", "2025-11-05")
            .param("checkOut", "2025-11-08"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("selectedRoomType", RoomType.SINGLE))
        .andExpect(model().attribute("maxPrice", new BigDecimal("120.00")))
        .andExpect(model().attribute("minCapacity", 1))
        .andExpect(model().attribute("checkIn", LocalDate.of(2025, 11, 5)))
        .andExpect(model().attribute("checkOut", LocalDate.of(2025, 11, 8)));
  }

  @Test
  void whenNoRoomsFound_thenReturnsEmptyList() throws Exception {
    when(authUtil.isAuthenticated()).thenReturn(false);
    when(roomService.findAvailableRooms()).thenReturn(List.of());

    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));

    verify(roomService).findAvailableRooms();
  }
}