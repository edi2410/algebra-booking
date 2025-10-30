package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.configs.TestSecurityConfig;
import hr.egraovac.alg.algebrabooking.exception.RoomNotFoundException;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class RoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtUtil jwtUtil;

  private Room testRoom;

  @BeforeEach
  void setUp() {
    testRoom = new Room();
    testRoom.setId(1L);
    testRoom.setRoomNumber("101");
    testRoom.setRoomType(RoomType.SINGLE);
    testRoom.setStatus(RoomStatus.AVAILABLE);
    testRoom.setPricePerNight(BigDecimal.valueOf(100.00));
    testRoom.setCapacity(2);
    testRoom.setDescription("Test room description");
  }

  // ==================== ROOM DETAILS (PUBLIC) ====================

  @Test
  void whenAccessingRoomDetails_thenSuccess() throws Exception {
    when(roomService.findById(1L)).thenReturn(testRoom);

    mockMvc.perform(get("/room/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("room/details"))
        .andExpect(model().attributeExists("room"))
        .andExpect(model().attribute("isRoomAvailable", true));

    verify(roomService, times(1)).findById(1L);
  }

  @Test
  @WithAnonymousUser
  void whenAccessingRoomDetails_UnavailableRoom_thenShowsUnavailable() throws Exception {
    testRoom.setStatus(RoomStatus.OCCUPIED);
    when(roomService.findById(1L)).thenReturn(testRoom);

    mockMvc.perform(get("/room/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("room/details"))
        .andExpect(model().attribute("isRoomAvailable", false));
  }

  @Test
  @WithAnonymousUser
  void whenAccessingNonExistentRoom_thenThrowsException() throws Exception {
    when(roomService.findById(999L)).thenThrow(new RoomNotFoundException(999L));

    mockMvc.perform(get("/room/999"))
        .andExpect(status().is5xxServerError());

    verify(roomService, times(1)).findById(999L);
  }

  // ==================== NEW ROOM (MANAGER ONLY) ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesNewRoomForm_thenSuccess() throws Exception {
    mockMvc.perform(get("/room/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("room/form"))
        .andExpect(model().attributeExists("room"))
        .andExpect(model().attributeExists("roomTypes"))
        .andExpect(model().attributeExists("roomStatuses"))
        .andExpect(model().attribute("isEdit", false))
        .andExpect(model().attribute("pageTitle", "Add New Room"));
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesNewRoomForm_thenForbidden() throws Exception {
    mockMvc.perform(get("/room/new"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesNewRoomForm_thenForbidden() throws Exception {
    mockMvc.perform(get("/room/new"))
        .andExpect(status().isForbidden());
  }

  @Test
  void whenUnauthenticatedUserAccessesNewRoomForm_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get("/room/new"))
        .andExpect(status().isForbidden());
  }

  // ==================== EDIT ROOM (MANAGER ONLY) ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesEditRoomForm_thenSuccess() throws Exception {
    when(roomService.findById(1L)).thenReturn(testRoom);

    mockMvc.perform(get("/room/edit/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("room/form"))
        .andExpect(model().attributeExists("room"))
        .andExpect(model().attributeExists("roomTypes"))
        .andExpect(model().attributeExists("roomStatuses"))
        .andExpect(model().attribute("isEdit", true))
        .andExpect(model().attribute("pageTitle", "Edit Room #101"));

    verify(roomService, times(1)).findById(1L);
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerAccessesEditNonExistentRoom_thenRedirectToHome() throws Exception {
    when(roomService.findById(999L)).thenReturn(null);

    mockMvc.perform(get("/room/edit/999"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));

    verify(roomService, times(1)).findById(999L);
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistAccessesEditRoomForm_thenForbidden() throws Exception {
    mockMvc.perform(get("/room/edit/1"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestAccessesEditRoomForm_thenForbidden() throws Exception {
    mockMvc.perform(get("/room/edit/1"))
        .andExpect(status().isForbidden());
  }

  // ==================== SAVE ROOM (MANAGER ONLY) ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerSavesNewRoom_thenSuccess() throws Exception {
    when(roomService.saveRoom(any(Room.class))).thenReturn(testRoom);

    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2")
            .param("description", "Test room"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/null"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(roomService, times(1)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerUpdatesExistingRoom_thenSuccess() throws Exception {
    testRoom.setId(5L);
    when(roomService.saveRoom(any(Room.class))).thenReturn(testRoom);

    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("id", "5")
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2")
            .param("description", "Updated room"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/5"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(roomService, times(1)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenSaveRoomFails_thenRedirectWithError() throws Exception {
    when(roomService.saveRoom(any(Room.class)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/new"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(roomService, times(1)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenUpdateRoomFails_thenRedirectToEditWithError() throws Exception {
    when(roomService.saveRoom(any(Room.class)))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("id", "5")
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/edit/5"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(roomService, times(1)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistTriesToSaveRoom_thenForbidden() throws Exception {
    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2"))
        .andExpect(status().isForbidden());

    verify(roomService, never()).saveRoom(any());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenSaveRoomWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/room/save")
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "2"))
        .andExpect(status().isForbidden());

    verify(roomService, never()).saveRoom(any());
  }

  // ==================== DELETE ROOM (MANAGER ONLY) ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenManagerDeletesRoom_thenSuccess() throws Exception {
    doNothing().when(roomService).deleteRoom(1L);

    mockMvc.perform(post("/room/delete/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(roomService, times(1)).deleteRoom(1L);
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenDeleteRoomFails_thenRedirectWithError() throws Exception {
    doThrow(new RuntimeException("Cannot delete room with active bookings"))
        .when(roomService).deleteRoom(1L);

    mockMvc.perform(post("/room/delete/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(roomService, times(1)).deleteRoom(1L);
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void whenReceptionistTriesToDeleteRoom_thenForbidden() throws Exception {
    mockMvc.perform(post("/room/delete/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(roomService, never()).deleteRoom(anyLong());
  }

  @Test
  @WithMockUser(username = "guest", roles = "GUEST")
  void whenGuestTriesToDeleteRoom_thenForbidden() throws Exception {
    mockMvc.perform(post("/room/delete/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(roomService, never()).deleteRoom(anyLong());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenDeleteRoomWithoutCsrf_thenForbidden() throws Exception {
    mockMvc.perform(post("/room/delete/1"))
        .andExpect(status().isForbidden());

    verify(roomService, never()).deleteRoom(anyLong());
  }

  @Test
  void whenUnauthenticatedUserTriesToDeleteRoom_thenRedirectToLogin() throws Exception {
    mockMvc.perform(post("/room/delete/1")
            .with(csrf()))
        .andExpect(status().isForbidden());
    verify(roomService, never()).deleteRoom(anyLong());
  }

  // ==================== EDGE CASES ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenSaveRoomWithAllRoomTypes_thenSuccess() throws Exception {
    for (RoomType roomType : RoomType.values()) {
      testRoom.setRoomType(roomType);
      when(roomService.saveRoom(any(Room.class))).thenReturn(testRoom);

      mockMvc.perform(post("/room/save")
              .with(csrf())
              .param("roomNumber", "101")
              .param("roomType", roomType.name())
              .param("status", "AVAILABLE")
              .param("pricePerNight", "100.00")
              .param("capacity", "2"))
          .andExpect(status().is3xxRedirection());
    }

    verify(roomService, times(RoomType.values().length)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenSaveRoomWithAllStatuses_thenSuccess() throws Exception {
    for (RoomStatus status : RoomStatus.values()) {
      testRoom.setStatus(status);
      when(roomService.saveRoom(any(Room.class))).thenReturn(testRoom);

      mockMvc.perform(post("/room/save")
              .with(csrf())
              .param("roomNumber", "101")
              .param("roomType", "SINGLE")
              .param("status", status.name())
              .param("pricePerNight", "100.00")
              .param("capacity", "2"))
          .andExpect(status().is3xxRedirection());
    }

    verify(roomService, times(RoomStatus.values().length)).saveRoom(any(Room.class));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void whenSaveRoomWithMinimalData_thenSuccess() throws Exception {
    when(roomService.saveRoom(any(Room.class))).thenReturn(testRoom);

    mockMvc.perform(post("/room/save")
            .with(csrf())
            .param("roomNumber", "101")
            .param("roomType", "SINGLE")
            .param("status", "AVAILABLE")
            .param("pricePerNight", "100.00")
            .param("capacity", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/null"));

    verify(roomService, times(1)).saveRoom(any(Room.class));
  }

}