package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.dto.BookingStatsDTO;
import hr.egraovac.alg.algebrabooking.dto.RevenueStatsDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.repository.BookingJdbcRepository;
import hr.egraovac.alg.algebrabooking.repository.BookingRepository;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BookingService {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private BookingJdbcRepository bookingJdbcRepository;

  /**
   * Save or update booking
   */
  @Transactional
  public Booking save(Booking booking) {
    return bookingRepository.save(booking);
  }

  /**
   * Find booking by ID
   */
  public Booking findById(Long id) {
    return bookingRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + id));
  }

  /**
   * Find bookings by guest ID
   */
  public List<Booking> findByGuestId(Long guestId) {
    return bookingRepository.findByGuestId(guestId);
  }

  /**
   * Find all bookings
   */
  public List<Booking> getAllBookings() {
    return bookingRepository.findAll();
  }

  /**
   * Check room availability
   */
  public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
    return bookingRepository.existsOverlappingBooking(roomId, checkIn, checkOut);
  }

  /**
   * Cancel booking (used by guest or staff)
   */
  @Transactional
  public void cancelBooking(Long id) {
    updateStatus(id, BookingStatus.CANCELLED, List.of(
        BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN
    ));
  }

  /**
   * Confirm pending booking
   */
  @Transactional
  public void confirmBooking(Long id) {
    updateStatus(id, BookingStatus.CONFIRMED, List.of(BookingStatus.PENDING));
  }

  /**
   * Check-in booking
   */
  @Transactional
  public void checkIn(Long id) {
    updateStatus(id, BookingStatus.CHECKED_IN, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
  }

  /**
   * Check-out booking
   */
  @Transactional
  public void checkOut(Long id) {
    updateStatus(id, BookingStatus.CHECKED_OUT, List.of(BookingStatus.CHECKED_IN));
    emailService.sendNewHousekeepingTask(
        findById(id)
    );

  }

  /**
   * Helper to update booking status safely
   */
  private void updateStatus(Long id, BookingStatus newStatus, List<BookingStatus> allowedCurrentStatuses) {
    Booking booking = findById(id);

    if (!allowedCurrentStatuses.contains(booking.getStatus())) {
      throw new IllegalStateException(
          String.format("Cannot change booking from %s to %s", booking.getStatus(), newStatus)
      );
    }

    booking.setStatus(newStatus);
    save(booking);
  }

  /**
   * Filter bookings dynamically by optional parameters
   */
  public List<Booking> searchBookings(BookingStatus status, LocalDate dateFrom, LocalDate dateTo, String guestName) {
    return bookingRepository.searchBookings(status, guestName).stream()
        .filter(b -> (dateFrom == null || !b.getCheckInDate().isBefore(dateFrom)))
        .filter(b -> (dateTo == null || !b.getCheckOutDate().isAfter(dateTo)))
        .collect(Collectors.toList());
  }

  /**
   * Get booking statistics (for dashboard)
   */
  public BookingStatsDTO getBookingStatistics() {
    long total = bookingRepository.count();
    long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
    long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
    long checkedIn = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
    long checkedOut = bookingRepository.countByStatus(BookingStatus.CHECKED_OUT);
    long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);

    return new BookingStatsDTO(total, pending, confirmed, checkedIn, checkedOut, cancelled);
  }

  /**
   * Get monthly revenue statistics
   */
  public List<RevenueStatsDTO> getMonthlyRevenue() {
    return bookingJdbcRepository.getMonthlyRevenue();
  }

  public List<Booking> getBookingsByStatusAndCheckInDate (BookingStatus bookingStatus, LocalDate checkInDate) {
    return bookingRepository.getBookingsByStatusAndCheckInDate( bookingStatus, checkInDate);
  }

}
