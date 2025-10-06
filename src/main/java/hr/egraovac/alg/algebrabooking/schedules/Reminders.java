package hr.egraovac.alg.algebrabooking.schedules;

import hr.egraovac.alg.algebrabooking.models.BookingReminder;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class Reminders {
//
//  @Scheduled(fixedRate = 3600000) // svaki sat
//  public void sendReminders() {
//    List<BookingReminder> pending = reminderRepo.findByScheduledTimeBeforeAndSentFalse(LocalDateTime.now());
//    pending.forEach(reminder -> {
//      emailService.send(reminder.getBooking().getGuest().getEmail(), "Reminder!");
//      reminder.setSent(true);
//    });
//  }
}
