package hr.egraovac.alg.algebrabooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RevenueStatsDTO {
  private int year;
  private int month;
  private BigDecimal revenue;
  private Long bookingCount;

}
