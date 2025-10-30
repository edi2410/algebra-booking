package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelSchedulerServiceTest {

  @Mock
  private BookingService bookingService;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private HotelSchedulerService hotelSchedulerService;

  private Booking booking1;
  private Booking booking2;
  private User guest1;
  private User guest2;

  @BeforeEach
  void setUp() {
    guest1 = new User();
    guest1.setEmail("guest1@test.com");

    guest2 = new User();
    guest2.setEmail("guest2@test.com");

    booking1 = new Booking();
    booking1.setGuest(guest1);
    booking1.setStatus(BookingStatus.CONFIRMED);

    booking2 = new Booking();
    booking2.setGuest(guest2);
    booking2.setStatus(BookingStatus.CONFIRMED);
  }

  @Test
  void sendBookingReminders_withUpcomingBookings_shouldSendEmails() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Booking> bookings = Arrays.asList(booking1, booking2);

    when(bookingService.getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow))
        .thenReturn(bookings);

    hotelSchedulerService.sendBookingReminders();

    verify(bookingService).getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow);
    verify(emailService).sendBookingReminder("guest1@test.com", booking1);
    verify(emailService).sendBookingReminder("guest2@test.com", booking2);
    verify(emailService, times(2)).sendBookingReminder(any(), any());
  }

  @Test
  void sendBookingReminders_withNoBookings_shouldNotSendEmails() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    when(bookingService.getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow))
        .thenReturn(Collections.emptyList());

    hotelSchedulerService.sendBookingReminders();

    verify(bookingService).getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow);
    verify(emailService, never()).sendBookingReminder(any(), any());
  }

  @Test
  void sendBookingReminders_withSingleBooking_shouldSendOneEmail() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Booking> bookings = Collections.singletonList(booking1);

    when(bookingService.getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow))
        .thenReturn(bookings);

    hotelSchedulerService.sendBookingReminders();

    verify(emailService).sendBookingReminder("guest1@test.com", booking1);
    verify(emailService, times(1)).sendBookingReminder(any(), any());
  }

  @Test
  void sendBookingReminders_shouldCheckTomorrowsDate() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    when(bookingService.getBookingsByStatusAndCheckInDate(any(), any()))
        .thenReturn(Collections.emptyList());

    hotelSchedulerService.sendBookingReminders();

    verify(bookingService).getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow);
  }

  @Test
  void sendBookingReminders_shouldOnlyCheckConfirmedStatus() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);

    when(bookingService.getBookingsByStatusAndCheckInDate(any(), any()))
        .thenReturn(Collections.emptyList());

    hotelSchedulerService.sendBookingReminders();

    verify(bookingService).getBookingsByStatusAndCheckInDate(eq(BookingStatus.CONFIRMED), any());
    verify(bookingService, never()).getBookingsByStatusAndCheckInDate(eq(BookingStatus.CANCELLED), any());
    verify(bookingService, never()).getBookingsByStatusAndCheckInDate(eq(BookingStatus.PENDING), any());
  }
}