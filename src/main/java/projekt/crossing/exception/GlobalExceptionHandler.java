package projekt.crossing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.model.SystemState;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObstacleDetectedException.class)
    public ResponseEntity<StatusResponse> handleObstacle(ObstacleDetectedException ex) {
        StatusResponse response = new StatusResponse(SystemState.EMERGENCY, ex.getMessage());
        // Zwracamy 409 Conflict, bo sytuacja na torach koliduje z zamknięciem
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HardwareFailureException.class)
    public ResponseEntity<StatusResponse> handleHardwareFailure(HardwareFailureException ex) {
        StatusResponse response = new StatusResponse(SystemState.ERROR, ex.getMessage());
        // Zwracamy 503 Service Unavailable, bo infrastruktura ma awarię
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}