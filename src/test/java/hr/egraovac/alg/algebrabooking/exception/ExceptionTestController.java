package hr.egraovac.alg.algebrabooking.exception;

import org.springframework.security.authorization.AuthorizationDeniedException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;

@RestController
class ExceptionTestController {

  @GetMapping("/runtime")
  public void runtimeException() {
    throw new RuntimeException("Test runtime exception");
  }

  @GetMapping("/generic")
  public void genericException() throws Exception {
    throw new Exception("Test generic exception");
  }

  @GetMapping("/auth")
  public void authException() {
    throw new AuthorizationDeniedException("Test forbidden");
  }

}

