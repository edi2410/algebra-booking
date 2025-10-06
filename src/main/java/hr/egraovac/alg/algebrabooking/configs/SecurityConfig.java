package hr.egraovac.alg.algebrabooking.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/room/**", "/register", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/api/**").permitAll()
            .requestMatchers("/guest/**").hasRole("GUEST")
            .requestMatchers("/receptionist/**").hasRole("RECEPTIONIST")
            .requestMatchers("/manager/**").hasRole("MANAGER")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
            .defaultSuccessUrl("/dashboard", true)
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/")
            .permitAll()
        );

    return http.build();
  }
}
