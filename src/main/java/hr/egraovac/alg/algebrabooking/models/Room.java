package hr.egraovac.alg.algebrabooking.models;


import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rooms")
public class Room {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String roomNumber;

  @Enumerated(EnumType.STRING)
  private RoomType roomType; // SINGLE, DOUBLE, SUITE

  @Column(nullable = false)
  private BigDecimal pricePerNight;

  private Integer capacity; // Number of guests

  @Enumerated(EnumType.STRING)
  private RoomStatus status; // AVAILABLE, OCCUPIED, MAINTENANCE

  private String description;

  @OneToMany(mappedBy = "room")
  private List<Booking> bookings;
}

