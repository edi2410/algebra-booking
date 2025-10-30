package hr.egraovac.alg.algebrabooking.controller.REST;

import hr.egraovac.alg.algebrabooking.dto.request.AuthRequest;
import hr.egraovac.alg.algebrabooking.dto.response.AuthResponse;
import hr.egraovac.alg.algebrabooking.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              authRequest.getUsername(),
              authRequest.getPassword()
          )
      );
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(401).body("Invalid username or password");
    }

    final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
    final String jwt = jwtUtil.generateToken(userDetails);

    return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername()));
  }

}