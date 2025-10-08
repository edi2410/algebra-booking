package hr.egraovac.alg.algebrabooking.service;

import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public void saveUser(User user) {
    userRepository.save(user);
  }
}
