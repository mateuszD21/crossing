package projekt.crossing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.exception.HardwareFailureException;
import projekt.crossing.exception.InvalidStateTransitionException;
import projekt.crossing.exception.ObstacleDetectedException;
import projekt.crossing.service.CrossingService;

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
        String message;
        try {
            message = switch (action) {
                case "train-approach"   -> crossingService.handleTrainApproach().getMessage();
                case "train-passed"     -> crossingService.handleTrainPassed().getMessage();
                case "emergency-stop"   -> crossingService.handleEmergencyStop().getMessage();
                case "emergency-open"   -> crossingService.emergencyOpen().getMessage();
                case "reset-emergency"  -> crossingService.resetFromEmergency().getMessage();
                case "reset-error"      -> crossingService.resetFromError().getMessage();
                case "obstacle"         -> { crossingService.handleObstacle(); yield ""; }
                case "hardware-failure" -> { crossingService.handleHardwareFailure(); yield ""; }
                default                 -> "Nieznana akcja.";
            };
        } catch (ObstacleDetectedException | HardwareFailureException e) {
            message = e.getMessage();
        } catch (InvalidStateTransitionException e) {
            message = "⛔ " + e.getMessage();
        }

        redirectAttributes.addFlashAttribute("lastMessage", message);
        return "redirect:/dashboard";
    }
}