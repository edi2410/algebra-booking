package hr.egraovac.alg.algebrabooking.models;

import hr.egraovac.alg.algebrabooking.utils.enums.ReminderType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_reminders")
public class BookingReminder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "booking_id", nullable = false)
  private Booking booking;

  @Enumerated(EnumType.STRING)
  private ReminderType reminderType; // CHECK_IN, CHECK_OUT, PAYMENT

  private LocalDateTime scheduledTime;
  private Boolean sent = false;
  private LocalDateTime sentAt;
}

