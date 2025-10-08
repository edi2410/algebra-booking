package hr.egraovac.alg.algebrabooking.dto;

import hr.egraovac.alg.algebrabooking.models.Booking;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

  private Long id;
  private String username;
  private String email;
  private String fullName;
  private String phone;
  private Set<UserRole> roles = new HashSet<>();
  private List<Booking> bookings;
}
