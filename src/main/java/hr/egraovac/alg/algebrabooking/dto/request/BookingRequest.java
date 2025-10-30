package hr.egraovac.alg.algebrabooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
  private Long roomId;
  private LocalDate checkInDate;
  private LocalDate checkOutDate;
  private String specialRequests;
}
