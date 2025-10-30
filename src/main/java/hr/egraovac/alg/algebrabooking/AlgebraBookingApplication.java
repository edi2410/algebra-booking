package hr.egraovac.alg.algebrabooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlgebraBookingApplication {

  public static void main(String[] args) {
    SpringApplication.run(AlgebraBookingApplication.class, args);
  }

}
