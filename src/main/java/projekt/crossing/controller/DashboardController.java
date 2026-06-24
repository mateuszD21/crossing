package projekt.crossing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.service.CrossingService;

import static projekt.crossing.service.CrossingService.log;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CrossingService crossingService;

    public DashboardController(CrossingService crossingService) {
        this.crossingService = crossingService;
    }

    @GetMapping
    public String dashboard(Model model) {
        StatusResponse status = crossingService.getSystemStatus();
        model.addAttribute("state", status.getState().name());
        model.addAttribute("history", crossingService.getHistory());
        return "dashboard";
    }

    @PostMapping("/action")
    public String handleAction(@RequestParam String action,
                               RedirectAttributes redirectAttributes) {
        try {
            switch (action) {
                case "train-approach"   -> crossingService.handleTrainApproach();
                case "train-passed"     -> crossingService.handleTrainPassed();
                case "emergency-stop"   -> crossingService.handleEmergencyStop();
                case "emergency-open"   -> crossingService.emergencyOpen();
                case "obstacle"         -> crossingService.handleObstacle();
                case "hardware-failure" -> crossingService.handleHardwareFailure();
            }
        } catch (Exception e) {
            log.warn("Akcja '{}' zakończona wyjątkiem: {}", action, e.getMessage());
        }
        return "redirect:/dashboard";
    }
}