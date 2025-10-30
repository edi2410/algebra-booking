package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.mapper.UserMapper;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BookingRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookingService bookingService;

  @MockitoBean
  private RoomService roomService;

  @MockitoBean
  private AuthUtil authUtil;

  @MockitoBean
  private UserService userService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User guestUser;
  private User otherGuestUser;
  private User receptionistUser;
  private User managerUser;
  private String guestToken;
  private String otherGuestToken;
  private String receptionistToken;
  private String managerToken;
  private Room room;
  private Booking booking;

  @BeforeEach
  void setUp() {
    // Guest user
    guestUser = new User();
    guestUser.setId(1L);
    guestUser.setUsername("guest");
    guestUser.setPassword(passwordEncoder.encode("password"));
    guestUser.setRoles(Set.of(UserRole.GUEST));

    // Other guest user
    otherGuestUser = new User();
    otherGuestUser.setId(2L);
    otherGuestUser.setUsername("otherguest");
    otherGuestUser.setPassword(passwordEncoder.encode("password"));
    otherGuestUser.setRoles(Set.of(UserRole.GUEST));

    // Receptionist user
    receptionistUser = new User();
    receptionistUser.setId(3L);
    receptionistUser.setUsername("receptionist");
    receptionistUser.setPassword(passwordEncoder.encode("password"));
    receptionistUser.setRoles(Set.of(UserRole.RECEPTIONIST));

    // Manager user
    managerUser = new User();
    managerUser.setId(4L);
    managerUser.setUsername("manager");
    managerUser.setPassword(passwordEncoder.encode("password"));
    managerUser.setRoles(Set.of(UserRole.MANAGER));

    // Room
    room = new Room();
    room.setId(1L);
    room.setRoomNumber("101");
    room.setStatus(RoomStatus.AVAILABLE);
    room.setPricePerNight(BigDecimal.valueOf(100));

    // Booking
    booking = new Booking();
    booking.setId(1L);
    booking.setGuest(guestUser);
    booking.setRoom(room);
    booking.setCheckInDate(LocalDate.now().plusDays(1));
    booking.setCheckOutDate(LocalDate.now().plusDays(3));
    booking.setTotalPrice(BigDecimal.valueOf(200));
    booking.setStatus(BookingStatus.PENDING);

    // Mock UserService
    when(userService.findByUsername("guest")).thenReturn(Optional.of(guestUser));
    when(userService.findByUsername("otherguest")).thenReturn(Optional.of(otherGuestUser));
    when(userService.findByUsername("receptionist")).thenReturn(Optional.of(receptionistUser));
    when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

    // Generate tokens
    guestToken = jwtUtil.generateToken(UserMapper.toUserDetails(guestUser));
    otherGuestToken = jwtUtil.generateToken(UserMapper.toUserDetails(otherGuestUser));
    receptionistToken = jwtUtil.generateToken(UserMapper.toUserDetails(receptionistUser));
    managerToken = jwtUtil.generateToken(UserMapper.toUserDetails(managerUser));
  }

  // ==================== CREATE BOOKING ====================

  @Test
  void createBooking_Success() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(true);
    when(bookingService.save(any())).thenReturn(booking);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"" + booking.getCheckInDate() +
                "\",\"checkOutDate\":\"" + booking.getCheckOutDate() + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(booking.getId()))
        .andExpect(jsonPath("$.guestId").value(guestUser.getId()))
        .andExpect(jsonPath("$.roomId").value(room.getId()))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(bookingService, times(1)).save(any(Booking.class));
  }

  @Test
  void createBooking_WithSpecialRequests() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(true);
    when(bookingService.save(any())).thenReturn(booking);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"" + booking.getCheckInDate() +
                "\",\"checkOutDate\":\"" + booking.getCheckOutDate() +
                "\",\"specialRequests\":\"Early check-in please\"}"))
        .andExpect(status().isCreated());

    verify(bookingService, times(1)).save(any(Booking.class));
  }

  @Test
  void createBooking_RoomNotFound() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(anyLong())).thenReturn(null);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":99,\"checkInDate\":\"2025-12-01\",\"checkOutDate\":\"2025-12-03\"}"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Room not found"));

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_RoomNotAvailableStatus() throws Exception {
    room.setStatus(RoomStatus.OCCUPIED);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"2025-12-01\",\"checkOutDate\":\"2025-12-03\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Room is not available"));

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_InvalidDates_CheckOutBeforeCheckIn() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"2025-12-05\",\"checkOutDate\":\"2025-12-03\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid check-in or check-out date"));

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_InvalidDates_CheckInInPast() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"2020-01-01\",\"checkOutDate\":\"2020-01-03\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid check-in or check-out date"));

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_RoomNotAvailableForDates() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(false);

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"2025-12-01\",\"checkOutDate\":\"2025-12-03\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Room is not available for the selected dates"));

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_WithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"2025-12-01\",\"checkOutDate\":\"2025-12-03\"}"))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).save(any());
  }

  @Test
  void createBooking_ServiceThrowsException() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(roomService.findById(1L)).thenReturn(room);
    when(bookingService.isRoomAvailable(anyLong(), any(), any())).thenReturn(true);
    when(bookingService.save(any())).thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(post("/api/bookings")
            .header("Authorization", "Bearer " + guestToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roomId\":1,\"checkInDate\":\"" + booking.getCheckInDate() +
                "\",\"checkOutDate\":\"" + booking.getCheckOutDate() + "\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Error creating booking")));
  }

  // ==================== GET BOOKING BY ID ====================

  @Test
  void getBooking_Success() throws Exception {
    when(bookingService.findById(1L)).thenReturn(booking);

    mockMvc.perform(get("/api/bookings/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(booking.getId()))
        .andExpect(jsonPath("$.guestId").value(guestUser.getId()))
        .andExpect(jsonPath("$.roomId").value(room.getId()));
  }

  @Test
  void getBooking_NotFound() throws Exception {
    when(bookingService.findById(999L)).thenReturn(null);

    mockMvc.perform(get("/api/bookings/999")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isNotFound());
  }

  @Test
  void getBooking_WithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/bookings/1"))
        .andExpect(status().isForbidden());
  }

  // ==================== GET MY BOOKINGS ====================

  @Test
  void getMyBookings_Success() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(bookingService.findByGuestId(guestUser.getId())).thenReturn(List.of(booking));

    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(booking.getId()))
        .andExpect(jsonPath("$[0].guestId").value(guestUser.getId()));

    verify(bookingService, times(1)).findByGuestId(guestUser.getId());
  }

  @Test
  void getMyBookings_EmptyList() throws Exception {
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(bookingService.findByGuestId(guestUser.getId())).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getMyBookings_MultipleBookings() throws Exception {
    Booking booking2 = new Booking();
    booking2.setId(2L);
    booking2.setGuest(guestUser);
    booking2.setRoom(room);
    booking2.setStatus(BookingStatus.CONFIRMED);

    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    when(bookingService.findByGuestId(guestUser.getId())).thenReturn(List.of(booking, booking2));

    mockMvc.perform(get("/api/bookings/my-bookings")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void getMyBookings_WithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/bookings/my-bookings"))
        .andExpect(status().isForbidden());
  }

  // ==================== GET ALL BOOKINGS (STAFF) ====================

  @Test
  void getAllBookings_ManagerSuccess() throws Exception {
    when(bookingService.searchBookings(null, null, null, null)).thenReturn(List.of(booking));

    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(booking.getId()));

    verify(bookingService, times(1)).searchBookings(null, null, null, null);
  }

  @Test
  void getAllBookings_ReceptionistSuccess() throws Exception {
    when(bookingService.searchBookings(null, null, null, null)).thenReturn(List.of(booking));

    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void getAllBookings_WithFilters() throws Exception {
    when(bookingService.searchBookings(
        eq(BookingStatus.CONFIRMED),
        eq(LocalDate.of(2025, 12, 1)),
        eq(LocalDate.of(2025, 12, 31)),
        eq("guest")
    )).thenReturn(List.of(booking));

    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + managerToken)
            .param("status", "CONFIRMED")
            .param("dateFrom", "2025-12-01")
            .param("dateTo", "2025-12-31")
            .param("guestName", "guest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));

    verify(bookingService).searchBookings(
        BookingStatus.CONFIRMED,
        LocalDate.of(2025, 12, 1),
        LocalDate.of(2025, 12, 31),
        "guest"
    );
  }

  @Test
  void getAllBookings_GuestForbidden() throws Exception {
    mockMvc.perform(get("/api/bookings/all")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).searchBookings(any(), any(), any(), any());
  }

  @Test
  void getAllBookings_WithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/bookings/all"))
        .andExpect(status().isForbidden());
  }

  // ==================== CANCEL BOOKING ====================

  @Test
  void cancelBooking_Success() throws Exception {
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    doNothing().when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/api/bookings/cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking cancelled successfully"));

    verify(bookingService, times(1)).cancelBooking(1L);
  }

  @Test
  void cancelBooking_Unauthorized_DifferentUser() throws Exception {
    booking.setGuest(otherGuestUser);
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/api/bookings/cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Unauthorized action"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  void cancelBooking_CannotCancelCheckedIn() throws Exception {
    booking.setStatus(BookingStatus.CHECKED_IN);
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/api/bookings/cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Cannot cancel checked-in/out booking"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  void cancelBooking_CannotCancelCheckedOut() throws Exception {
    booking.setStatus(BookingStatus.CHECKED_OUT);
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);

    mockMvc.perform(post("/api/bookings/cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Cannot cancel checked-in/out booking"));

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  void cancelBooking_ServiceThrowsException() throws Exception {
    when(bookingService.findById(1L)).thenReturn(booking);
    when(authUtil.extractAuthorizedUser()).thenReturn(guestUser);
    doThrow(new RuntimeException("Database error")).when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/api/bookings/cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Error cancelling booking")));
  }

  @Test
  void cancelBooking_WithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/bookings/cancel/1"))
        .andExpect(status().isForbidden());
  }

  // ==================== CHECK-IN BOOKING (STAFF) ====================

  @Test
  void checkInBooking_ManagerSuccess() throws Exception {
    doNothing().when(bookingService).checkIn(1L);

    mockMvc.perform(post("/api/bookings/check-in/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking checked in successfully"));

    verify(bookingService, times(1)).checkIn(1L);
  }

  @Test
  void checkInBooking_ReceptionistSuccess() throws Exception {
    doNothing().when(bookingService).checkIn(1L);

    mockMvc.perform(post("/api/bookings/check-in/1")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking checked in successfully"));

    verify(bookingService, times(1)).checkIn(1L);
  }

  @Test
  void checkInBooking_GuestForbidden() throws Exception {
    mockMvc.perform(post("/api/bookings/check-in/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).checkIn(anyLong());
  }

  @Test
  void checkInBooking_WithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/bookings/check-in/1"))
        .andExpect(status().isForbidden());
  }

  // ==================== CHECK-OUT BOOKING (STAFF) ====================

  @Test
  void checkOutBooking_ManagerSuccess() throws Exception {
    doNothing().when(bookingService).checkOut(1L);

    mockMvc.perform(post("/api/bookings/check-out/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking checked out successfully"));

    verify(bookingService, times(1)).checkOut(1L);
  }

  @Test
  void checkOutBooking_ReceptionistSuccess() throws Exception {
    doNothing().when(bookingService).checkOut(1L);

    mockMvc.perform(post("/api/bookings/check-out/1")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk());

    verify(bookingService, times(1)).checkOut(1L);
  }

  @Test
  void checkOutBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Room not cleaned")).when(bookingService).checkOut(1L);

    mockMvc.perform(post("/api/bookings/check-out/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Error checking out booking")));
  }

  @Test
  void checkOutBooking_GuestForbidden() throws Exception {
    mockMvc.perform(post("/api/bookings/check-out/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).checkOut(anyLong());
  }

  // ==================== CONFIRM BOOKING (STAFF) ====================

  @Test
  void confirmBooking_ManagerSuccess() throws Exception {
    doNothing().when(bookingService).confirmBooking(1L);

    mockMvc.perform(post("/api/bookings/confirm/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking confirmed successfully"));

    verify(bookingService, times(1)).confirmBooking(1L);
  }

  @Test
  void confirmBooking_ReceptionistSuccess() throws Exception {
    doNothing().when(bookingService).confirmBooking(1L);

    mockMvc.perform(post("/api/bookings/confirm/1")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk());

    verify(bookingService, times(1)).confirmBooking(1L);
  }

  @Test
  void confirmBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Payment not received")).when(bookingService).confirmBooking(1L);

    mockMvc.perform(post("/api/bookings/confirm/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Error confirming booking")));
  }

  @Test
  void confirmBooking_GuestForbidden() throws Exception {
    mockMvc.perform(post("/api/bookings/confirm/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).confirmBooking(anyLong());
  }

  // ==================== STAFF CANCEL BOOKING ====================

  @Test
  void staffCancelBooking_ManagerSuccess() throws Exception {
    doNothing().when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/api/bookings/staff-cancel/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isOk())
        .andExpect(content().string("Booking cancelled successfully"));

    verify(bookingService, times(1)).cancelBooking(1L);
  }

  @Test
  void staffCancelBooking_ReceptionistSuccess() throws Exception {
    doNothing().when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/api/bookings/staff-cancel/1")
            .header("Authorization", "Bearer " + receptionistToken))
        .andExpect(status().isOk());

    verify(bookingService, times(1)).cancelBooking(1L);
  }

  @Test
  void staffCancelBooking_ServiceThrowsException() throws Exception {
    doThrow(new RuntimeException("Cannot cancel")).when(bookingService).cancelBooking(1L);

    mockMvc.perform(post("/api/bookings/staff-cancel/1")
            .header("Authorization", "Bearer " + managerToken))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Error cancelling booking")));
  }

  @Test
  void staffCancelBooking_GuestForbidden() throws Exception {
    mockMvc.perform(post("/api/bookings/staff-cancel/1")
            .header("Authorization", "Bearer " + guestToken))
        .andExpect(status().isForbidden());

    verify(bookingService, never()).cancelBooking(anyLong());
  }

  @Test
  void staffCancelBooking_WithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/bookings/staff-cancel/1"))
        .andExpect(status().isForbidden());
  }
}