package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.dto.BookingDTO;
import hr.egraovac.alg.algebrabooking.dto.request.BookingRequest;
import hr.egraovac.alg.algebrabooking.mapper.BookingMapper;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Management", description = "APIs for managing hotel room bookings")
public class BookingRestController {

  @Autowired
  private BookingService bookingService;
  @Autowired
  private RoomService roomService;
  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  @Operation(summary = "Create a new booking", description = "Creates a new room booking for the authenticated user")
  public ResponseEntity<?> createBooking(
      @RequestBody BookingRequest bookingRequest
  ) {
    try {
      User guest = authUtil.extractAuthorizedUser();
      Room room = roomService.findById(bookingRequest.getRoomId());

      LocalDate checkOutDate = bookingRequest.getCheckOutDate();
      LocalDate checkInDate = bookingRequest.getCheckInDate();

      if (room == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Room not found");
      }

      if (room.getStatus() != RoomStatus.AVAILABLE) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Room is not available");
      }

      if (checkOutDate.isBefore(checkInDate) || checkInDate.isBefore(LocalDate.now())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Invalid check-in or check-out date");
      }

      if (!bookingService.isRoomAvailable(bookingRequest.getRoomId(), checkInDate, checkOutDate)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Room is not available for the selected dates");
      }

      long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
      BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

      Booking booking = new Booking();
      booking.setGuest(guest);
      booking.setRoom(room);
      booking.setCheckInDate(checkInDate);
      booking.setCheckOutDate(checkOutDate);
      booking.setTotalPrice(totalPrice);
      booking.setStatus(BookingStatus.PENDING);
      booking.setSpecialRequests(bookingRequest.getSpecialRequests());

      Booking savedBooking = bookingService.save(booking);

      return ResponseEntity.status(HttpStatus.CREATED).body(BookingMapper.toDTO(savedBooking));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error creating booking: " + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get booking by ID", description = "Retrieves a specific booking by its unique identifier")
  public ResponseEntity<?> getBooking(@PathVariable Long id) {
    Booking booking = bookingService.findById(id);
    if (booking == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(BookingMapper.toDTO(booking));
  }

  @GetMapping("/my-bookings")
  @Operation(summary = "Get current user's bookings", description = "Retrieves all bookings made by the authenticated user")
  public ResponseEntity<List<BookingDTO>> getMyBookings() {
    User guest = authUtil.extractAuthorizedUser();
    List<Booking> bookings = bookingService.findByGuestId(guest.getId());
    return ResponseEntity.ok(BookingMapper.toListOfDTO(bookings));
  }

  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('MANAGER','RECEPTIONIST')")
  @Operation(summary = "Get all bookings with filters", description = "Retrieves all bookings with optional filtering. Requires MANAGER or RECEPTIONIST role")
  public ResponseEntity<List<BookingDTO>> getAllBookings(
      @RequestParam(required = false) BookingStatus status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String guestName
  ) {
    List<Booking> bookings = bookingService.searchBookings(
        status,
        dateFrom,
        dateTo,
        guestName
    );
    return ResponseEntity.ok(BookingMapper.toListOfDTO(bookings));
  }

  @PostMapping("/cancel/{id}")
  @Operation(summary = "Cancel own booking", description = "Allows a guest to cancel their own booking")
  public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
    try {
      Booking booking = bookingService.findById(id);
      User user = authUtil.extractAuthorizedUser();

      if (!booking.getGuest().getUsername().equals(user.getUsername())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("Unauthorized action");
      }

      if (booking.getStatus() == BookingStatus.CHECKED_IN ||
          booking.getStatus() == BookingStatus.CHECKED_OUT) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Cannot cancel checked-in/out booking");
      }

      bookingService.cancelBooking(id);
      return ResponseEntity.ok("Booking cancelled successfully");

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error cancelling booking: " + e.getMessage());
    }
  }

  @PostMapping("/check-in/{id}")
  @PreAuthorize("hasAnyRole('MANAGER','RECEPTIONIST')")
  @Operation(summary = "Check-in booking", description = "Marks a booking as checked-in. Requires MANAGER or RECEPTIONIST role")
  public ResponseEntity<?> checkInBooking(@PathVariable Long id) {
    bookingService.checkIn(id);
    return ResponseEntity.ok("Booking checked in successfully");
  }

  @PostMapping("/check-out/{id}")
  @PreAuthorize("hasAnyRole('MANAGER','RECEPTIONIST')")
  @Operation(summary = "Check-out booking", description = "Marks a booking as checked-out. Requires MANAGER or RECEPTIONIST role")
  public ResponseEntity<?> checkOutBooking(@PathVariable Long id) {
    try {
      bookingService.checkOut(id);
      return ResponseEntity.ok("Booking checked out successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error checking out booking: " + e.getMessage());
    }
  }

  @PostMapping("/confirm/{id}")
  @PreAuthorize("hasAnyRole('MANAGER','RECEPTIONIST')")
  @Operation(summary = "Confirm booking", description = "Confirms a pending booking. Requires MANAGER or RECEPTIONIST role")
  public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
    try {
      bookingService.confirmBooking(id);
      return ResponseEntity.ok("Booking confirmed successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error confirming booking: " + e.getMessage());
    }
  }

  @PostMapping("/staff-cancel/{id}")
  @PreAuthorize("hasAnyRole('MANAGER','RECEPTIONIST')")
  @Operation(summary = "Staff cancel booking", description = "Allows staff to cancel any booking. Requires MANAGER or RECEPTIONIST role")
  public ResponseEntity<?> staffCancelBooking(@PathVariable Long id) {
    try {
      bookingService.cancelBooking(id);
      return ResponseEntity.ok("Booking cancelled successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error cancelling booking: " + e.getMessage());
    }
  }
}