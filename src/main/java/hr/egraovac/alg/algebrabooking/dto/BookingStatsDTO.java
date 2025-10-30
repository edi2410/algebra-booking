package hr.egraovac.alg.algebrabooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDTO {
  private long total;
  private long pending;
  private long confirmed;
  private long checkedIn;
  private long checkedOut;
  private long cancelled;
}