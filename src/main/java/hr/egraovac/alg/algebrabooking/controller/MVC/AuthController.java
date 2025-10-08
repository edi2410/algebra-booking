package hr.egraovac.alg.algebrabooking.controller.MVC;

import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.UserService;
import hr.egraovac.alg.algebrabooking.utils.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;


  @GetMapping("/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String showRegisterForm() {
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(@RequestParam String username,
      @RequestParam String email,
      @RequestParam String password,
      @RequestParam(required = false) String fullName,
      Model model) {

    if (userService.findByUsername(username).isPresent()) {
      model.addAttribute("error", "Username already exists");
      return "auth/register";
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setFullName(fullName);
    user.setPassword(passwordEncoder.encode(password));
    user.addRole(UserRole.GUEST);
    userService.saveUser(user);

    return "redirect:/login?registered";
  }
}
