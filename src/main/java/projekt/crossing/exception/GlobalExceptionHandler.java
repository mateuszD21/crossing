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
        // 409 Conflict - przeszkoda koliduje z procedurą zamknięcia
        return new ResponseEntity<>(
                new StatusResponse(SystemState.EMERGENCY, ex.getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HardwareFailureException.class)
    public ResponseEntity<StatusResponse> handleHardwareFailure(HardwareFailureException ex) {
        // 503 Service Unavailable - infrastruktura niesprawna
        return new ResponseEntity<>(
                new StatusResponse(SystemState.ERROR, ex.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<StatusResponse> handleInvalidTransition(InvalidStateTransitionException ex) {
        // 422 Unprocessable Entity - żądana operacja jest logicznie niemożliwa w bieżącym stanie
        return new ResponseEntity<>(
                new StatusResponse(null, ex.getMessage()),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}