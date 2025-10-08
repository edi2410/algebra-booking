package hr.egraovac.alg.algebrabooking.dto;

import hr.egraovac.alg.algebrabooking.utils.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

  private Long id;
  private Long guestId;
  private Long roomId;
  private LocalDate checkInDate;
  private LocalDate checkOutDate;
  private BigDecimal totalPrice;
  private BookingStatus status;
  private String specialRequests;
}
