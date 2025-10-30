package hr.egraovac.alg.algebrabooking.utils;

import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {


  @Autowired
  private UserService userService;

  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null &&
        authentication.isAuthenticated() &&
        !(authentication instanceof AnonymousAuthenticationToken);
  }

  public User extractAuthorizedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.findByUsername(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
  }

  public String extractUsernameFromAuthorizedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

}


