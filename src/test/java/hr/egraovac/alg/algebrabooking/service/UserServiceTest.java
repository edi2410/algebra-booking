package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    user = new User();
    user.setId(1L);
    user.setUsername("john");
    user.setPassword("password");
  }

  @Test
  void findByUsername_WhenUserExists_ShouldReturnUser() {
    when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

    Optional<User> result = userService.findByUsername("john");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("john");
    verify(userRepository, times(1)).findByUsername("john");
  }

  @Test
  void findByUsername_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    Optional<User> result = userService.findByUsername("nonexistent");

    assertThat(result).isEmpty();
    verify(userRepository, times(1)).findByUsername("nonexistent");
  }

  @Test
  void saveUser_ShouldCallRepositorySave() {
    userService.saveUser(user);

    verify(userRepository, times(1)).save(user);
  }
}
