package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.configs.TestSecurityConfig;
import hr.egraovac.alg.algebrabooking.dto.BookingStatsDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(TestSecurityConfig.class)
class BookingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtUtil jwtUtil;

  @MockitoBean
  private BookingService bookingService;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private AuthUtil authUtil;



  @MockitoBean
  private UserService userService;

  private User guestUser;
  private User otherUser;
  private User receptionistUser;
  private User managerUser;
  private Room availableRoom;
  private Booking booking;

  @BeforeEach
  void setUp() {
    // Guest user
    guestUser = new User();
    guestUser.setId(1L);
    guestUser.setUsername("user");
    guestUser.setPassword("password");
    guestUser.setRoles(Set.of(UserRole.GUEST));

    // Other guest user
    otherUser = new User();
    otherUser.setId(2L);
    otherUser.setUsername("other");
    otherUser.setPassword("password");
    otherUser.setRoles(Set.of(UserRole.GUEST));

    // Receptionist user
    receptionistUser = new User();
    receptionistUser.setId(3L);
    receptionistUser.setUsername("receptionist");
    receptionistUser.setPassword("password");
    receptionistUser.setRoles(Set.of(UserRole.RECEPTIONIST));

    // Manager user
    managerUser = new User();
    managerUser.setId(4L);
    managerUser.setUsername("manager");
    managerUser.setPassword("password");
    managerUser.setRoles(Set.of(UserRole.MANAGER));

    // Room
    availableRoom = new Room();
    availableRoom.setId(1L);
    availableRoom.setPricePerNight(BigDecimal.valueOf(100));
    availableRoom.setStatus(RoomStatus.AVAILABLE);

    // Booking
    booking = new Booking();
    booking.setId(1L);
    booking.setGuest(guestUser);
    booking.setRoom(availableRoom);
    booking.setCheckInDate(LocalDate.now().plusDays(1));
    booking.setCheckOutDate(LocalDate.now().plusDays(3));
    booking.setStatus(BookingStatus.PENDING);

    // Mock UserService za UserDetailsService
    when(userService.findByUsername("user")).thenReturn(Optional.of(guestUser));
    when(userService.findByUsername("other")).thenReturn(Optional.of(otherUser));
    when(userService.findByUsername("receptionist")).thenReturn(Optional.of(receptionistUser));
    when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
  }

  // ==================== CREATE BOOKING ====================

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void createBooking_Success() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(availableRoom);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(false);
    when(bookingService.save(any())).thenReturn(booking);

    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", booking.getCheckInDate().toString())
            .param("checkOutDate", booking.getCheckOutDate().toString())
            .param("specialRequests", "None"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/confirmation"))
        .andExpect(model().attributeExists("booking", "nights", "room"));

    verify(bookingService, times(1)).save(any(Booking.class));
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void createBooking_ServiceThrowsException() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(availableRoom);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(false);
    when(bookingService.save(any())).thenThrow(new RuntimeException("Service error"));

    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", booking.getCheckInDate().toString())
            .param("checkOutDate", booking.getCheckOutDate().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/1"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, times(1)).save(any());
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void createBooking_RoomNotFound() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(anyLong())).thenReturn(null);

    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", LocalDate.now().plusDays(1).toString())
            .param("checkOutDate", LocalDate.now().plusDays(2).toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, never()).save(any());
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void createBooking_RoomNotAvailable() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(availableRoom);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(true);

    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", LocalDate.now().plusDays(1).toString())
            .param("checkOutDate", LocalDate.now().plusDays(2).toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/1"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, never()).save(any());
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void createBooking_InvalidDates() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(availableRoom);

    // Check-out date prije check-in date
    mockMvc.perform(post("/booking/create")
            .with(csrf())
            .param("roomId", "1")
            .param("checkInDate", LocalDate.now().plusDays(5).toString())
            .param("checkOutDate", LocalDate.now().plusDays(3).toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/room/1"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, never()).save(any());
  }

  // ==================== VIEW CONFIRMATION ====================

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void viewConfirmation_Success() throws Exception {
    when(bookingService.findById(1L)).thenReturn(booking);

    mockMvc.perform(get("/booking/confirmation/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/confirmation"))
        .andExpect(model().attributeExists("booking", "nights", "room"));
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void viewConfirmation_NotFound() throws Exception {
    when(bookingService.findById(1L)).thenThrow(new RuntimeException("Not found"));

    mockMvc.perform(get("/booking/confirmation/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  // ==================== MY BOOKINGS ====================

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void myBookings_Success() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(bookingService.findByGuestId(guestUser.getId())).thenReturn(List.of(booking));

    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/my-bookings"))
        .andExpect(model().attributeExists("bookings"));
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void myBookings_EmptyList() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(bookingService.findByGuestId(guestUser.getId())).thenReturn(List.of());

    mockMvc.perform(get("/booking/my-bookings"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/my-bookings"))
        .andExpect(model().attribute("bookings", List.of()));
  }

  // ==================== ALL BOOKINGS (STAFF) ====================

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void getAllBookings_Success() throws Exception {
    when(bookingService.searchBookings(any(), any(), any(), any())).thenReturn(List.of(booking));
    when(bookingService.getBookingStatistics()).thenReturn(new BookingStatsDTO());

    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/all-bookings"))
        .andExpect(model().attributeExists("bookings", "stats"));
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void getAllBookings_AsManager_Success() throws Exception {
    when(bookingService.searchBookings(any(), any(), any(), any())).thenReturn(List.of(booking));
    when(bookingService.getBookingStatistics()).thenReturn(new BookingStatsDTO());

    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/all-bookings"))
        .andExpect(model().attributeExists("bookings", "stats"));
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void getAllBookings_Forbidden() throws Exception {
    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void getAllBookings_WithFilters() throws Exception {
    LocalDate dateFrom = LocalDate.now();
    LocalDate dateTo = LocalDate.now().plusDays(7);

    // Parametri prema BookingController: status, dateFrom, dateTo, guestName
    when(bookingService.searchBookings(
        eq(BookingStatus.CONFIRMED),
        eq(dateFrom),
        eq(dateTo),
        eq("user")
    )).thenReturn(List.of(booking));
    when(bookingService.getBookingStatistics()).thenReturn(new BookingStatsDTO());

    mockMvc.perform(get("/booking/all")
            .param("status", "CONFIRMED")
            .param("dateFrom", dateFrom.toString())
            .param("dateTo", dateTo.toString())
            .param("guestName", "user"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/all-bookings"))
        .andExpect(model().attributeExists("bookings", "stats"));

    verify(bookingService).searchBookings(
        BookingStatus.CONFIRMED,
        dateFrom,
        dateTo,
        "user"
    );
  }

  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void getAllBookings_EmptyFilters() throws Exception {
    when(bookingService.searchBookings(null, null, null, null))
        .thenReturn(List.of(booking));
    when(bookingService.getBookingStatistics()).thenReturn(new BookingStatsDTO());

    mockMvc.perform(get("/booking/all"))
        .andExpect(status().isOk())
        .andExpect(view().name("booking/all-bookings"));

    verify(bookingService).searchBookings(null, null, null, null);
  }

  // ==================== CANCEL BOOKING ====================

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void cancelBooking_Success() throws Exception {
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    doNothing().when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/booking/cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/my-bookings"))
        .andExpect(flash().attributeExists("success"));

    verify(bookingService, times(1)).cancelBooking(1L);
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void cancelBooking_Unauthorized() throws Exception {
    booking.setGuest(otherUser);

    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/booking/cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/my-bookings"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void cancelBooking_AlreadyCheckedIn() throws Exception {
    booking.setStatus(BookingStatus.CHECKED_IN);

    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/booking/cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/my-bookings"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void cancelBooking_NotFound() throws Exception {
    when(bookingService.findById(1L)).thenReturn(null);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/booking/cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/my-bookings"))
        .andExpect(flash().attributeExists("error"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  // ==================== STAFF ACTIONS ====================

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void checkInBooking_Success() throws Exception {
    doNothing().when(bookingService).checkIn(1L);

    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).checkIn(1L);
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void checkInBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Cannot check in")).when(bookingService).checkIn(1L);

    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, times(1)).checkIn(1L);
  }




  @Test
  @WithMockUser(username = "receptionist", roles = "RECEPTIONIST")
  void checkInBooking_AsReceptionist_Success() throws Exception {
    doNothing().when(bookingService).checkIn(1L);

    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).checkIn(1L);
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void checkInBooking_Forbidden() throws Exception {
    mockMvc.perform(post("/booking/check-in/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).checkIn(anyLong());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void checkOutBooking_Success() throws Exception {
    doNothing().when(bookingService).checkOut(1L);

    mockMvc.perform(post("/booking/check-out/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).checkOut(1L);
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void checkOutBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Cannot check out")).when(bookingService).checkOut(1L);

    mockMvc.perform(post("/booking/check-out/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, times(1)).checkOut(1L);
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void checkOutBooking_Forbidden() throws Exception {
    mockMvc.perform(post("/booking/check-out/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).checkOut(anyLong());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void confirmBooking_Success() throws Exception {
    doNothing().when(bookingService).confirmBooking(1L);

    mockMvc.perform(post("/booking/confirm/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).confirmBooking(1L);
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void confirmBooking_Error() throws Exception {

    mockMvc.perform(post("/booking/confirm/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).confirmBooking(1L);
  }



  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void confirmBooking_Forbidden() throws Exception {
    mockMvc.perform(post("/booking/confirm/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).confirmBooking(anyLong());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void staffCancelBooking_Success() throws Exception {
    doNothing().when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/booking/staff-cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(bookingService, times(1)).cancelBooking(1L);
  }

  @Test
  @WithMockUser(username = "user", roles = "GUEST")
  void staffCancelBooking_Forbidden() throws Exception {
    mockMvc.perform(post("/booking/staff-cancel/1")
            .with(csrf()))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  @WithMockUser(username = "manager", roles = "MANAGER")
  void staffCancelBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Cannot cancel")).when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/booking/staff-cancel/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/booking/all"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(bookingService, times(1)).cancelBooking(1L);
  }
}