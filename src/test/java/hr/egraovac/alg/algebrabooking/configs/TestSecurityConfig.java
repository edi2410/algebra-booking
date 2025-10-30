package hr.egraovac.alg.algebrabooking.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@Import(SecurityConfig.class)
public class TestSecurityConfig {
}
