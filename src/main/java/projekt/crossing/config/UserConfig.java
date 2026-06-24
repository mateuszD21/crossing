package projekt.crossing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}