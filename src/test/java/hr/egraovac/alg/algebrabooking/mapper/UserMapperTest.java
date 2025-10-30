package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.UserDTO;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("john_doe");
    user.setPassword("password123");
    user.setEmail("john@example.com");
    user.setFullName("John Doe");
    user.setPhone("+123456789");
    user.setRoles(Set.of(UserRole.GUEST, UserRole.MANAGER));
    user.setBookings(List.of());
  }


  @Test
  void toDto_ShouldMapAllFieldsCorrectly() {
    UserDTO dto = UserMapper.toDto(user);

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getUsername()).isEqualTo("john_doe");
    assertThat(dto.getEmail()).isEqualTo("john@example.com");
    assertThat(dto.getFullName()).isEqualTo("John Doe");
    assertThat(dto.getPhone()).isEqualTo("+123456789");
    assertThat(dto.getRoles()).containsExactlyInAnyOrder(UserRole.GUEST, UserRole.MANAGER);
  }

  @Test
  void toDto_WhenUserIsNull_ShouldReturnNull() {
    UserDTO dto = UserMapper.toDto(null);
    assertThat(dto).isNull();
  }


  @Test
  void toDtoList_ShouldMapListOfUsersToListOfDTOs() {
    User another = new User();
    another.setId(2L);
    another.setUsername("jane_doe");
    another.setRoles(Set.of(UserRole.RECEPTIONIST));

    List<UserDTO> dtoList = UserMapper.toDtoList(List.of(user, another));

    assertThat(dtoList).hasSize(2);
    assertThat(dtoList.get(0).getUsername()).isEqualTo("john_doe");
    assertThat(dtoList.get(1).getUsername()).isEqualTo("jane_doe");
  }

  @Test
  void toUserDetails_ShouldConvertUserToSpringUserDetails() {
    UserDetails userDetails = UserMapper.toUserDetails(user);

    assertThat(userDetails.getUsername()).isEqualTo("john_doe");
    assertThat(userDetails.getPassword()).isEqualTo("password123");

    assertThat(userDetails.getAuthorities())
        .extracting("authority")
        .containsExactlyInAnyOrder("ROLE_GUEST", "ROLE_MANAGER");
  }
}
