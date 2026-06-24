package projekt.crossing.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.crossing.model.User;
import projekt.crossing.repository.UserRepository;
import projekt.crossing.security.TotpService;

@Controller
@RequestMapping("/totp")
public class TotpController {

    private static final String SESSION_TOTP_OK      = "TOTP_VERIFIED";
    private static final String SESSION_SETUP_SECRET = "TOTP_SETUP_SECRET";

    private final TotpService    totpService;
    private final UserRepository userRepository;

    public TotpController(TotpService totpService, UserRepository userRepository) {
        this.totpService    = totpService;
        this.userRepository = userRepository;
    }

    @GetMapping("/setup")
    public String setupPage(@AuthenticationPrincipal UserDetails principal,
                            HttpSession session,
                            Model model) {

        User user = findUser(principal.getUsername());

        if (user.isTotpEnabled()) {
            return "redirect:/totp/verify";
        }

        String secret = (String) session.getAttribute(SESSION_SETUP_SECRET);
        if (secret == null) {
            secret = totpService.generateSecret();
            session.setAttribute(SESSION_SETUP_SECRET, secret);
        }

        model.addAttribute("qrBase64", totpService.getQrBase64(user.getUsername(), secret, "SystemRogatki"));
        model.addAttribute("secret", secret);
        return "totp-setup";
    }

    @PostMapping("/setup/confirm")
    public String confirmSetup(@AuthenticationPrincipal UserDetails principal,
                               @RequestParam String code,
                               HttpSession session,
                               RedirectAttributes ra) {

        String secret = (String) session.getAttribute(SESSION_SETUP_SECRET);
        if (secret == null) return "redirect:/totp/setup";

        if (!totpService.verifyCode(secret, code)) {
            ra.addFlashAttribute("error", "Nieprawidłowy kod. Spróbuj ponownie.");
            return "redirect:/totp/setup";
        }

        User user = findUser(principal.getUsername());
        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        userRepository.save(user);

        session.removeAttribute(SESSION_SETUP_SECRET);
        session.setAttribute(SESSION_TOTP_OK, true);

        ra.addFlashAttribute("success", "Uwierzytelnianie dwuetapowe zostało włączone!");
        return "redirect:/dashboard";
    }

    @GetMapping("/verify")
    public String verifyPage(HttpSession session) {
        if (Boolean.TRUE.equals(session.getAttribute(SESSION_TOTP_OK))) {
            return "redirect:/dashboard";
        }
        return "totp-verify";
    }

    @PostMapping("/verify")
    public String verifyCode(@AuthenticationPrincipal UserDetails principal,
                             @RequestParam String code,
                             HttpSession session,
                             RedirectAttributes ra) {

        User user = findUser(principal.getUsername());

        if (!user.isTotpEnabled()) {
            return "redirect:/totp/setup";
        }

        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            ra.addFlashAttribute("error", "Nieprawidłowy kod. Spróbuj ponownie.");
            return "redirect:/totp/verify";
        }

        session.setAttribute(SESSION_TOTP_OK, true);
        return "redirect:/dashboard";
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony: " + username));
    }
}
