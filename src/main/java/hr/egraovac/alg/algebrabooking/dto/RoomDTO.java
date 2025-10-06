package hr.egraovac.alg.algebrabooking.dto;

import hr.egraovac.alg.algebrabooking.utils.enums.RoomStatus;
import hr.egraovac.alg.algebrabooking.utils.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {

  private Long id;
  private String roomNumber;
  private RoomType roomType;
  private BigDecimal pricePerNight;
  private Integer capacity;
  private RoomStatus status;
  private String description;
}