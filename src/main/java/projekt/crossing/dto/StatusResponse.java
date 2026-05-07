package projekt.crossing.dto;

import projekt.crossing.model.SystemState;
import java.time.LocalDateTime;

public class StatusResponse {
    private SystemState state;
    private String message;
    private LocalDateTime timestamp;

    public StatusResponse(SystemState state, String message) {
        this.state = state;
        this.message = message;
        this.timestamp = LocalDateTime.now(); // Zapisanie czasu aktywacji
    }

    // Gettery i Settery (lub użyj adnotacji @Data z biblioteki Lombok, jeśli macie ją w projekcie)
    public SystemState getState() { return state; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}