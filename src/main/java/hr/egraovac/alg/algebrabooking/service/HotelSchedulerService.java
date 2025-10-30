package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class HotelSchedulerService {

  @Autowired
  private BookingService bookingService;

  @Autowired
  private EmailService emailService;

  @Scheduled(cron = "0 0 10 * * ?")
  public void sendBookingReminders() {
    log.info("Starting booking reminder task...");

    LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Booking> upcomingBookings = bookingService.getBookingsByStatusAndCheckInDate(BookingStatus.CONFIRMED, tomorrow);

    for (Booking booking : upcomingBookings) {
      emailService.sendBookingReminder(
          booking.getGuest().getEmail(),
          booking
      );
    }

    log.info("Sent {} booking reminders", upcomingBookings.size());
  }

}
