package projekt.crossing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "crossing_events")
public class CrossingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemState toState;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected CrossingEvent() {}

    public CrossingEvent(SystemState fromState, SystemState toState, String message) {
        this.fromState = fromState;
        this.toState = toState;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public SystemState getFromState() { return fromState; }
    public SystemState getToState() { return toState; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}