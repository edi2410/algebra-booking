package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.mapper.UserMapper;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RoomRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  private User guestUser;
  private User managerUser;
  private Room room1;
  private Room room2;
  private String guestToken;
  private String managerToken;

  @BeforeEach
  void setUp() {
    // Guest user
    guestUser = new User();
    guestUser.setId(1L);
    guestUser.setUsername("guest");
    guestUser.setPassword(passwordEncoder.encode("password"));
    guestUser.setRoles(Set.of(UserRole.GUEST));

    // Manager user
    managerUser = new User();
    managerUser.setId(3L);
    managerUser.setUsername("manager");
    managerUser.setPassword(passwordEncoder.encode("password"));
    managerUser.setRoles(Set.of(UserRole.MANAGER));

    room1 = new Room();
    room1.setId(1L);
    room1.setRoomNumber("101");
    room1.setRoomType(RoomType.SINGLE);
    room1.setPricePerNight(new BigDecimal("100.00"));
    room1.setCapacity(1);
    room1.setStatus(RoomStatus.AVAILABLE);

    room2 = new Room();
    room2.setId(2L);
    room2.setRoomNumber("102");
    room2.setRoomType(RoomType.DOUBLE);
    room2.setPricePerNight(new BigDecimal("150.00"));
    room2.setCapacity(2);
    room2.setStatus(RoomStatus.AVAILABLE);

    // Mock UserService
    when(userService.findByUsername("guest")).thenReturn(Optional.of(guestUser));
    when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

    // Generate tokens
    guestToken = jwtUtil.generateToken(UserMapper.toUserDetails(guestUser));
    managerToken = jwtUtil.generateToken(UserMapper.toUserDetails(managerUser));

  }

  @Test
  void getAllRooms_ShouldReturnListOfRooms() throws Exception {
    when(roomService.allRooms()).thenReturn(List.of(room1, room2));

    mockMvc.perform(get("/api/rooms"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].roomNumber").value("102"));
  }

  @Test
  void getRooms_WithoutFilters_ShouldCallFindAvailableRooms() throws Exception {
    when(roomService.findAvailableRooms()).thenReturn(List.of(room1));

    mockMvc.perform(get("/api/rooms/filter")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1));

    verify(roomService).findAvailableRooms();
    verify(roomService, never()).searchRooms(any(), any(), any(), any(), any(), any());
  }

  @Test
  void getRooms_WithFilters_ShouldCallSearchRooms() throws Exception {
    when(roomService.searchRooms(any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(room2));

    mockMvc.perform(get("/api/rooms/filter")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .param("roomType", "DOUBLE")
            .param("maxPrice", "200")
            .param("minCapacity", "1")
            .param("checkIn", LocalDate.now().toString())
            .param("checkOut", LocalDate.now().plusDays(2).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(2));

    verify(roomService).searchRooms(eq(RoomStatus.AVAILABLE),
        eq(RoomType.DOUBLE),
        eq(new BigDecimal("200")),
        eq(1),
        any(LocalDate.class),
        any(LocalDate.class));
  }

  @Test
  void getRoomById_Found_ShouldReturnRoom() throws Exception {
    when(roomService.findById(1L)).thenReturn(room1);

    mockMvc.perform(get("/api/rooms/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomNumber").value("101"));
  }

  @Test
  void getRoomById_NotFound_ShouldReturn404() throws Exception {
    when(roomService.findById(99L)).thenReturn(null);

    mockMvc.perform(get("/api/rooms/99"))
        .andExpect(status().isNotFound());
  }

  @Test
  void createNewRoom_Success_ShouldReturnCreatedRoom() throws Exception {
    when(roomService.saveRoom(any(Room.class))).thenReturn(room1);

    mockMvc.perform(post("/api/rooms")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "roomNumber": "101",
                  "roomType": "SINGLE",
                  "pricePerNight": 100.00,
                  "capacity": 1,
                  "status": "AVAILABLE"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomNumber").value("101"));
  }

  @Test
  void createNewRoom_Failure_ShouldReturn500() throws Exception {
    when(roomService.saveRoom(any(Room.class))).thenThrow(new RuntimeException("DB error"));

    mockMvc.perform(post("/api/rooms")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                   {
                          "roomNumber": "999",
                          "roomType": "SUITE",
                          "pricePerNight": 500.00,
                          "capacity": 4,
                          "status": "AVAILABLE"
                        }
                """))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void updateRoom_Found_ShouldReturnUpdatedRoom() throws Exception {
    when(roomService.findById(1L)).thenReturn(room1);
    when(roomService.saveRoom(any(Room.class))).thenReturn(room1);

    mockMvc.perform(put("/api/rooms/1")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "roomNumber": "201",
                  "roomType": "DOUBLE",
                  "pricePerNight": 200.00,
                  "capacity": 2,
                  "status": "OCCUPIED"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomNumber").value("201"));
  }

  @Test
  void updateRoom_NotFound_ShouldReturn404() throws Exception {
    when(roomService.findById(1L)).thenReturn(null);

    mockMvc.perform(put("/api/rooms/1")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "roomNumber": "201",
                  "roomType": "DOUBLE",
                  "pricePerNight": 200.00,
                  "capacity": 2,
                  "status": "OCCUPIED"
                }
                """))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteRoom_Found_ShouldReturn204() throws Exception {
    when(roomService.findById(1L)).thenReturn(room1);
    doNothing().when(roomService).deleteRoom(1L);

    mockMvc.perform(delete("/api/rooms/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isNoContent());

    verify(roomService).deleteRoom(1L);
  }

  @Test
  void deleteRoom_NotFound_ShouldReturn404() throws Exception {
    when(roomService.findById(99L)).thenReturn(null);

    mockMvc.perform(delete("/api/rooms/99").header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isNotFound());
  }
}
