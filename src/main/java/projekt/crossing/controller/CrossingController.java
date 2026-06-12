package projekt.crossing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.service.CrossingService;

import java.util.List;

@RestController
@RequestMapping("/api/crossing")
public class CrossingController {

    private final CrossingService crossingService;

    public CrossingController(CrossingService crossingService) {
        this.crossingService = crossingService;
    }

    @PostMapping("/train-approach")
    public ResponseEntity<StatusResponse> handleTrainApproach() {
        return ResponseEntity.ok(crossingService.handleTrainApproach());
    }

    @PostMapping("/train-passed")
    public ResponseEntity<StatusResponse> handleTrainPassed() {
        return ResponseEntity.ok(crossingService.handleTrainPassed());
    }

    @PostMapping("/obstacle")
    public ResponseEntity<StatusResponse> handleObstacle() {
        return ResponseEntity.ok(crossingService.handleObstacle());
    }

    @PostMapping("/emergency-stop")
    public ResponseEntity<StatusResponse> emergencyStop() {
        return ResponseEntity.ok(crossingService.handleEmergencyStop());
    }

    @PostMapping("/hardware-failure")
    public ResponseEntity<StatusResponse> hardwareFailure() {
        return ResponseEntity.ok(crossingService.handleHardwareFailure());
    }

    @PostMapping("/emergency-open")
    public ResponseEntity<StatusResponse> emergencyOpen() {
        return ResponseEntity.ok(crossingService.emergencyOpen());
    }

    @PostMapping("/reset/emergency")
    public ResponseEntity<StatusResponse> resetFromEmergency() {
        return ResponseEntity.ok(crossingService.resetFromEmergency());
    }

    @PostMapping("/reset/error")
    public ResponseEntity<StatusResponse> resetFromError() {
        return ResponseEntity.ok(crossingService.resetFromError());
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(crossingService.getSystemStatus());
    }

    @GetMapping("/history")
    public ResponseEntity<List<CrossingEvent>> getHistory() {
        return ResponseEntity.ok(crossingService.getHistory());
    }
}