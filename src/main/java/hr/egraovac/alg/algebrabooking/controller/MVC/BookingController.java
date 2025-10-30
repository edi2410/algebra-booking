package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.Room;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.BookingService;
import hr.egraovac.alg.algebrabooking.service.RoomService;
import hr.egraovac.alg.algebrabooking.utils.AuthUtil;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

  @Autowired
  private BookingService bookingService;
  @Autowired
  private RoomService roomService;
  @Autowired
  private AuthUtil authUtil;


  @PostMapping("/create")
  public String createBooking(@RequestParam Long roomId,
      @RequestParam LocalDate checkInDate,
      @RequestParam LocalDate checkOutDate,
      @RequestParam(required = false) String specialRequests,
      RedirectAttributes redirectAttributes,
      Model model) {
    try {
      User guest = authUtil.extractAuthorizedUser();
      Room room = roomService.findById(roomId);

      if (room == null) return redirectError("/", redirectAttributes, "Room not found");
      if (room.getStatus() != RoomStatus.AVAILABLE)
        return redirectError("/room/" + roomId, redirectAttributes, "Room is not available");

      if (checkOutDate.isBefore(checkInDate) || checkInDate.isBefore(LocalDate.now()))
        return redirectError("/room/" + roomId, redirectAttributes, "Invalid check-in or check-out date");

      if (bookingService.isRoomAvailable(roomId, checkInDate, checkOutDate))
        return redirectError("/room/" + roomId, redirectAttributes, "Room is not available for the selected dates");

      long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
      if (nights <= 0)
        return redirectError("/room/" + roomId, redirectAttributes, "Check-out date must be after check-in date");

      BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

      Booking booking = new Booking();
      booking.setGuest(guest);
      booking.setRoom(room);
      booking.setCheckInDate(checkInDate);
      booking.setCheckOutDate(checkOutDate);
      booking.setTotalPrice(totalPrice);
      booking.setStatus(BookingStatus.PENDING);
      booking.setSpecialRequests(specialRequests);

      model.addAttribute("booking", bookingService.save(booking));
      model.addAttribute("nights", nights);
      model.addAttribute("room", room);

      return "booking/confirmation";

    } catch (Exception e) {
      return redirectError("/room/" + roomId, redirectAttributes, "Error creating booking: " + e.getMessage());
    }
  }

  @GetMapping("/confirmation/{id}")
  public String viewConfirmation(@PathVariable Long id, Model model) {
    try {
      Booking booking = bookingService.findById(id);
      long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
      model.addAttribute("booking", booking);
      model.addAttribute("nights", nights);
      model.addAttribute("room", booking.getRoom());
      return "booking/confirmation";
    } catch (Exception e) {
      return "redirect:/";
    }
  }


  @GetMapping("/my-bookings")
  public String myBookings(Model model) {
    User guest = authUtil.extractAuthorizedUser();
    model.addAttribute("bookings", bookingService.findByGuestId(guest.getId()));
    return "booking/my-bookings";
  }


  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
  public String getAllBookings(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String guestName,
      Model model) {

    List<Booking> bookings = bookingService.searchBookings(
        status != null && !status.isEmpty() ? BookingStatus.valueOf(status) : null,
        dateFrom != null && !dateFrom.isEmpty() ? LocalDate.parse(dateFrom) : null,
        dateTo != null && !dateTo.isEmpty() ? LocalDate.parse(dateTo) : null,
        guestName != null && !guestName.isEmpty() ? guestName : null
    );

    model.addAttribute("bookings", bookings);
    model.addAttribute("stats", bookingService.getBookingStatistics());
    return "booking/all-bookings";
  }

  @PostMapping("/cancel/{id}")
  public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      Booking booking = bookingService.findById(id);
      User user = authUtil.extractAuthorizedUser();

      if (!booking.getGuest().getUsername().equals(user.getUsername()))
        return redirectError("/booking/my-bookings", redirectAttributes, "Unauthorized action");

      if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT)
        return redirectError("/booking/my-bookings", redirectAttributes, "Cannot cancel checked-in/out booking");

      bookingService.cancelBooking(id);
      redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }
    return "redirect:/booking/my-bookings";
  }

  @PostMapping("/check-in/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
  public String checkInBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      bookingService.checkIn(id);
      redirectAttributes.addFlashAttribute("successMessage", "Booking checked in successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error checking in booking: " + e.getMessage());
    }
    return "redirect:/booking/all";
  }


  @PostMapping("/check-out/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
  public String checkOutBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      bookingService.checkOut(id);
      redirectAttributes.addFlashAttribute("successMessage", "Booking checked out successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error checking out booking: " + e.getMessage());
    }
    return "redirect:/booking/all";
  }

  @PostMapping("/confirm/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
  public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      bookingService.confirmBooking(id);
      redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error confirming booking: " + e.getMessage());
    }
    return "redirect:/booking/all";
  }

  @PostMapping("/staff-cancel/{id}")
  @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
  public String staffCancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      bookingService.cancelBooking(id);
      redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling booking: " + e.getMessage());
    }
    return "redirect:/booking/all";
  }

  private String redirectError(String path, RedirectAttributes redirectAttributes, String message) {
    redirectAttributes.addFlashAttribute("errorMessage", message);
    return "redirect:" + path;
  }
}
