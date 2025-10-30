package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.dto.BookingStatsDTO;
import hr.egraovac.alg.algebrabooking.dto.RevenueStatsDTO;
import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.repository.BookingJdbcRepository;
import hr.egraovac.alg.algebrabooking.repository.BookingRepository;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private EmailService emailService;

  @Mock
  private BookingJdbcRepository bookingJdbcRepository;

  @InjectMocks
  private BookingService bookingService;

  private Booking booking;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    booking = new Booking();
    booking.setId(1L);
    booking.setStatus(BookingStatus.PENDING);
    booking.setCheckInDate(LocalDate.of(2025, 10, 1));
    booking.setCheckOutDate(LocalDate.of(2025, 10, 5));
  }


  @Test
  void save_ShouldCallRepositorySave() {
    when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

    Booking result = bookingService.save(booking);

    verify(bookingRepository, times(1)).save(booking);
    assertThat(result).isEqualTo(booking);
  }

  @Test
  void findById_WhenExists_ShouldReturnBooking() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    Booking result = bookingService.findById(1L);

    assertThat(result).isEqualTo(booking);
  }

  @Test
  void findById_WhenNotExists_ShouldThrowException() {
    when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.findById(1L))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("Booking not found");
  }

  @Test
  void findByGuestId_ShouldReturnList() {
    when(bookingRepository.findByGuestId(5L)).thenReturn(List.of(booking));

    List<Booking> result = bookingService.findByGuestId(5L);

    assertThat(result).containsExactly(booking);
  }

  @Test
  void getAllBookings_ShouldReturnAll() {
    when(bookingRepository.findAll()).thenReturn(List.of(booking));

    List<Booking> result = bookingService.getAllBookings();

    assertThat(result).hasSize(1);
  }

  @Test
  void isRoomAvailable_ShouldReturnTrueOrFalse() {
    when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(true);
    boolean result = bookingService.isRoomAvailable(1L, LocalDate.now(), LocalDate.now().plusDays(2));

    assertThat(result).isTrue();
    verify(bookingRepository).existsOverlappingBooking(anyLong(), any(), any());
  }

  // =================== STATUS TRANSITIONS ===================

  @Test
  void cancelBooking_ValidStatus_ShouldUpdateToCancelled() {
    booking.setStatus(BookingStatus.PENDING);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.cancelBooking(1L);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
  }

  @Test
  void confirmBooking_ValidStatus_ShouldUpdateToConfirmed() {
    booking.setStatus(BookingStatus.PENDING);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.confirmBooking(1L);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
  }

  @Test
  void checkIn_ValidStatus_ShouldUpdateToCheckedIn() {
    booking.setStatus(BookingStatus.CONFIRMED);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.checkIn(1L);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
  }

  @Test
  void checkOut_ValidStatus_ShouldUpdateToCheckedOutAndSendEmail() {
    booking.setStatus(BookingStatus.CHECKED_IN);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any())).thenReturn(booking);

    bookingService.checkOut(1L);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CHECKED_OUT);
    verify(emailService, times(1)).sendNewHousekeepingTask(booking);
  }

  @Test
  void updateStatus_InvalidTransition_ShouldThrowException() {
    booking.setStatus(BookingStatus.CANCELLED);
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.confirmBooking(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot change booking from CANCELLED to CONFIRMED");
  }

  // =================== SEARCH ===================

  @Test
  void searchBookings_ShouldFilterByDateRange() {
    Booking b1 = new Booking();
    b1.setCheckInDate(LocalDate.of(2025, 10, 2));
    b1.setCheckOutDate(LocalDate.of(2025, 10, 5));

    when(bookingRepository.searchBookings(BookingStatus.PENDING, "John"))
        .thenReturn(List.of(booking, b1));

    List<Booking> result = bookingService.searchBookings(
        BookingStatus.PENDING,
        LocalDate.of(2025, 10, 1),
        LocalDate.of(2025, 10, 6),
        "John"
    );

    assertThat(result).hasSize(2);
  }

  // =================== STATISTICS ===================

  @Test
  void getBookingStatistics_ShouldReturnAggregatedCounts() {
    when(bookingRepository.count()).thenReturn(10L);
    when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(2L);
    when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(3L);
    when(bookingRepository.countByStatus(BookingStatus.CHECKED_IN)).thenReturn(2L);
    when(bookingRepository.countByStatus(BookingStatus.CHECKED_OUT)).thenReturn(2L);
    when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(1L);

    BookingStatsDTO stats = bookingService.getBookingStatistics();

    assertThat(stats.getTotal()).isEqualTo(10L);
    assertThat(stats.getPending()).isEqualTo(2L);
    assertThat(stats.getCancelled()).isEqualTo(1L);
  }

  @Test
  void getMonthlyRevenue_ShouldReturnListOfDTOs() {
    List<RevenueStatsDTO> mockStats = List.of(
        new RevenueStatsDTO(2025, 10, BigDecimal.valueOf(5000), 5L)
    );
    when(bookingJdbcRepository.getMonthlyRevenue()).thenReturn(mockStats);

    List<RevenueStatsDTO> result = bookingService.getMonthlyRevenue();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getMonth()).isEqualTo(10);
    assertThat(result.getFirst().getYear()).isEqualTo(2025);
    assertThat(result.getFirst().getRevenue()).isEqualTo(BigDecimal.valueOf(5000));
    assertThat(result.getFirst().getBookingCount()).isEqualTo(5L);
  }

  // =================== CUSTOM QUERY ===================

  @Test
  void getBookingsByStatusAndCheckInDate_ShouldDelegateToRepository() {
    when(bookingRepository.getBookingsByStatusAndCheckInDate(any(), any()))
        .thenReturn(List.of(booking));

    List<Booking> result = bookingService.getBookingsByStatusAndCheckInDate(
        BookingStatus.PENDING, LocalDate.now());

    assertThat(result).containsExactly(booking);
  }
}
