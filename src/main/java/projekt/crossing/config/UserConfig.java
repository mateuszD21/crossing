package projekt.crossing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Konfiguracja użytkowników systemu.
 * Dwie role: OPERATOR (odczyt + reset) i ADMIN (pełny dostęp).
 * W produkcji zastąpić bazą danych użytkowników.
 */
@Configuration
public class UserConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("operator")
                        .password(encoder.encode("operator123"))
                        .roles("OPERATOR")
                        .build(),
                User.withUsername("admin")
                        .password(encoder.encode("admin123"))
                        .roles("ADMIN", "OPERATOR")
                        .build()
        );
    }
}