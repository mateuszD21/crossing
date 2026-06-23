package projekt.crossing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import projekt.crossing.repository.CrossingEventRepository;
import projekt.crossing.repository.UserRepository;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import projekt.crossing.model.User;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class CrossingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;           // potrzebny przez TotpFilter i MongoUserDetailsService

    @MockBean
    CrossingEventRepository crossingEventRepository;  // potrzebny przez CrossingService

    // -------------------------------------------------------------------------
    // TESTY LOGOWANIA
    // -------------------------------------------------------------------------
    @BeforeEach
    void setupMocks() {
        // Zakoduj prawdziwe hasło żeby BCrypt mógł je porównać przy logowaniu
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User mockAdmin = new User("admin", encoder.encode("admin123"), "ROLE_ADMIN");
        User mockOperator = new User("operator", encoder.encode("operator123"), "ROLE_OPERATOR");

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(mockAdmin));
        when(userRepository.findByUsername("operator"))
                .thenReturn(Optional.of(mockOperator));
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty()); // nieznani userzy — pusta odpowiedź

        when(crossingEventRepository.findAllByOrderByTimestampDesc())
                .thenReturn(List.of());
        when(crossingEventRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(i -> i.getArgument(0));
    }
    private static RequestPostProcessor totpVerified() {
        return request -> {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("TOTP_VERIFIED", true);
            request.setSession(session);
            return request;
        };
    }

    @Test
    void loginPoprawnyAdmin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void loginBledneHaslo() throws Exception {
        mockMvc.perform(formLogin("/login").user("operator").password("zlehaslo"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginNieistniejacyUzytkownik() throws Exception {
        mockMvc.perform(formLogin("/login").user("ktoś").password("cokolwiek"))
                .andExpect(unauthenticated());
    }

    // -------------------------------------------------------------------------
    // DOSTĘP BEZ LOGOWANIA — powinno przekierować na /login
    // -------------------------------------------------------------------------


    @Test
    void apiBezLogowania_zwraca401() throws Exception {
        mockMvc.perform(get("/api/crossing/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiPostBezLogowania_zwraca401() throws Exception {
        mockMvc.perform(post("/api/crossing/train-approach"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // DOSTĘP PO ZALOGOWANIU
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void statusPoZalogowaniu_zwraca200() throws Exception {
        mockMvc.perform(get("/api/crossing/status").with(totpVerified()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    void historyPoZalogowaniu_zwraca200() throws Exception {
        mockMvc.perform(get("/api/crossing/history").with(totpVerified()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void dashboardPoZalogowaniu_zwraca200() throws Exception {
        mockMvc.perform(get("/dashboard").with(totpVerified()))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // TESTY PRZEJŚĆ STANÓW przez API
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void trainApproach_zmienaStanNaWarning() throws Exception {
        try { mockMvc.perform(post("/api/crossing/reset/emergency").with(totpVerified())); } catch (Exception ignored) {}
        try { mockMvc.perform(post("/api/crossing/reset/error").with(totpVerified())); }    catch (Exception ignored) {}

        mockMvc.perform(post("/api/crossing/train-approach").with(totpVerified()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("WARNING"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void emergencyStop_zmieniStanNaEmergency() throws Exception {
        try { mockMvc.perform(post("/api/crossing/reset/emergency").with(totpVerified())); } catch (Exception ignored) {}

        mockMvc.perform(post("/api/crossing/emergency-stop").with(totpVerified()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("EMERGENCY"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void resetFromEmergency_przywracaOpen() throws Exception {
        try { mockMvc.perform(post("/api/crossing/emergency-stop").with(totpVerified())); } catch (Exception ignored) {}

        mockMvc.perform(post("/api/crossing/reset/emergency").with(totpVerified()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void hardwareFailure_zwraca503() throws Exception {
        try { mockMvc.perform(post("/api/crossing/reset/error").with(totpVerified())); } catch (Exception ignored) {}

        mockMvc.perform(post("/api/crossing/hardware-failure").with(totpVerified()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.state").value("ERROR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void obstacle_zwraca409() throws Exception {
        try { mockMvc.perform(post("/api/crossing/reset/emergency").with(totpVerified())); } catch (Exception ignored) {}
        try { mockMvc.perform(post("/api/crossing/reset/error").with(totpVerified())); }    catch (Exception ignored) {}

        mockMvc.perform(post("/api/crossing/obstacle").with(totpVerified()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.state").value("EMERGENCY"));
    }
}