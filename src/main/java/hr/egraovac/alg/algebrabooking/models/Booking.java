package hr.egraovac.alg.algebrabooking.models;

import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "guest_id", nullable = false)
  private User guest;

  @ManyToOne
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Column(nullable = false)
  private LocalDate checkInDate;

  @Column(nullable = false)
  private LocalDate checkOutDate;

  @Column(nullable = false)
  private BigDecimal totalPrice;

  @Enumerated(EnumType.STRING)
  private BookingStatus status; // PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED

  private String specialRequests;

  @CreatedDate
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
  private List<BookingReminder> reminders;
}