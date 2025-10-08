package hr.egraovac.alg.algebrabooking.mapper;

import hr.egraovac.alg.algebrabooking.dto.UserDTO;
import hr.egraovac.alg.algebrabooking.models.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

  public static UserDTO toDto(User user) {
    if (user == null) {
      return null;
    }

    UserDTO userDTO = new UserDTO();
    userDTO.setId(user.getId());
    userDTO.setUsername(user.getUsername());
    userDTO.setEmail(user.getEmail());
    userDTO.setFullName(user.getFullName());
    userDTO.setPhone(user.getPhone());
    userDTO.setRoles(user.getRoles());
    userDTO.setBookings(user.getBookings());

    return userDTO;
  }

  public static List<UserDTO> toDtoList(List<User> users) {
    return users.stream().map(UserMapper::toDto).collect(Collectors.toList());

  }

}
