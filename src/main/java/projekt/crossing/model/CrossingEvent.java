package projekt.crossing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "crossing_events")
public class CrossingEvent {

    @Id
    private String id;

    private SystemState fromState;
    private SystemState toState;
    private String message;
    private LocalDateTime timestamp;

    protected CrossingEvent() {}

    public CrossingEvent(SystemState fromState, SystemState toState, String message) {
        this.fromState = fromState;
        this.toState = toState;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public SystemState getFromState() { return fromState; }
    public SystemState getToState() { return toState; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}