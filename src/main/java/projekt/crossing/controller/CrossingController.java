package projekt.crossing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projekt.crossing.service.CrossingService;
import projekt.crossing.dto.StatusResponse;

@RestController
@RequestMapping("/api/crossing")
public class CrossingController {

    private final CrossingService crossingService;

    public CrossingController(CrossingService crossingService) {
        this.crossingService = crossingService;
    }

    @PostMapping("/train-approach")
    public ResponseEntity<StatusResponse> handleTrainApproach() {
        StatusResponse response = crossingService.handleTrainApproach();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/obstacle")
    public ResponseEntity<StatusResponse> handleObstacle() {
        StatusResponse response = crossingService.handleObstacle();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/emergency-stop")
    public ResponseEntity<StatusResponse> emergencyStop() {
        StatusResponse response = crossingService.handleEmergencyStop();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        StatusResponse response = crossingService.getSystemStatus();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/hardware-failure")
    public ResponseEntity<StatusResponse> HardwareFailure(){
        StatusResponse response = crossingService.handleHardwareFailure();
        return ResponseEntity.ok(response);
    }
}