package projekt.crossing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import projekt.crossing.model.User;
import projekt.crossing.repository.UserRepository;

import java.io.IOException;
import java.util.Set;

/**
 * Filtr sprawdzający drugi etap logowania (TOTP).
 *
 * Działa po uwierzytelnieniu Spring Security.
 * Jeśli użytkownik ma włączone 2FA i jeszcze go nie potwierdził w tej sesji,
 * przekierowuje go na /totp/verify (lub /totp/setup jeśli 2FA nie skonfigurowane).
 */
@Component
public class TotpFilter extends OncePerRequestFilter {

    private static final String SESSION_TOTP_OK = "TOTP_VERIFIED";

    // Ścieżki zwolnione z weryfikacji TOTP
    private static final Set<String> BYPASS_PATHS = Set.of(
            "/totp/setup", "/totp/setup/confirm",
            "/totp/verify",
            "/login", "/logout",
            "/css"
    );

    private final UserRepository userRepository;

    public TotpFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Nie dotyczy anonimowych / niezalogowanych
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();

        // Pomiń ścieżki zwolnione
        if (BYPASS_PATHS.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean totpOk = session != null
                && Boolean.TRUE.equals(session.getAttribute(SESSION_TOTP_OK));

        if (totpOk) {
            chain.doFilter(request, response);
            return;
        }

        // Sprawdź, czy użytkownik ma 2FA w DB
        String username = auth.getName();
        boolean needsTotp = userRepository.findByUsername(username)
                .map(User::isTotpEnabled)
                .orElse(false);

        if (needsTotp) {
            response.sendRedirect(request.getContextPath() + "/totp/verify");
        } else {
            // 2FA nie skonfigurowane — wymuś setup
            response.sendRedirect(request.getContextPath() + "/totp/setup");
        }
    }
}
