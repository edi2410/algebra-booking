package hr.egraovac.alg.algebrabooking.configs;

import hr.egraovac.alg.algebrabooking.filter.JwtAuthenticationFilter;
import hr.egraovac.alg.algebrabooking.models.User;
import hr.egraovac.alg.algebrabooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Security Filter Chain for REST API (JWT-based)
   * Order(1) means this will be evaluated first
   */
  @Bean
  @Order(1)
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**") // Only apply to /api/** endpoints
        .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions for API
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll() // Public auth endpoints
            .requestMatchers("/api/guest/**").hasRole("GUEST")
            .requestMatchers("/api/receptionist/**").hasRole("RECEPTIONIST")
            .requestMatchers("/api/manager/**").hasRole("MANAGER")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Security Filter Chain for MVC (Session-based)
   * Order(2) means this will be evaluated after API filter chain
   */
  @Bean
  @Order(2)
  public SecurityFilterChain mvcFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/**") // Apply to all other endpoints
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/room/**",
                "/register",
                "/login",
                "/swagger-ui.html",
                "/h2-console/**"
            ).permitAll()
            .requestMatchers("/guest/**").hasRole("GUEST")
            .requestMatchers("/receptionist/**").hasRole("RECEPTIONIST")
            .requestMatchers("/manager/**").hasRole("MANAGER")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
            .defaultSuccessUrl("/dashboard", true)
            .failureUrl("/login?error=true")
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/h2-console/**")
        )
        .headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public UserDetailsService userDetailsService(UserService userService) {
    return username -> {
      Optional<User> optionalUser = userService.findByUsername(username);

      User user = optionalUser.orElseThrow(() ->
          new UsernameNotFoundException("User not found: " + username));

      String[] roles = user.getRoles().stream()
          .map(Enum::name)
          .toArray(String[]::new);

      return org.springframework.security.core.userdetails.User.builder()
          .username(user.getUsername())
          .password(user.getPassword())
          .roles(roles)
          .build();
    };
  }
}