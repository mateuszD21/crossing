package projekt.crossing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.exception.HardwareFailureException;
import projekt.crossing.exception.InvalidStateTransitionException;
import projekt.crossing.exception.ObstacleDetectedException;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.model.SystemState;
import projekt.crossing.repository.CrossingEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CrossingService {

    public static final Logger log = LoggerFactory.getLogger(CrossingService.class);

    private static final int CLOSING_SIMULATION_SECONDS = 5;
    private static final int OPENING_SIMULATION_SECONDS = 5;

    private SystemState currentState = SystemState.OPEN;

    private final CrossingEventRepository eventRepository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public CrossingService(CrossingEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .data(new StatusResponse(currentState, "Połączono. Stan: " + currentState)));
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    private void notifyClients() {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .data(new StatusResponse(currentState, "Zmiana stanu: " + currentState)));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }


    private synchronized void transition(SystemState target, String message) {
        if (!currentState.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(currentState, target);
        }
        SystemState previous = currentState;
        currentState = target;
        eventRepository.save(new CrossingEvent(previous, target, message));
        log.info("Zmiana stanu: {} -> {} | {}", previous, target, message);
        notifyClients();
    }

    public synchronized StatusResponse handleTrainApproach() {
        String msg = "Wykryto pociąg. Uruchomiono sygnalizację. Rogatki zamkną się za "
                + CLOSING_SIMULATION_SECONDS + "s.";
        transition(SystemState.WARNING, msg);

        scheduler.schedule(() -> {
            try { autoClose(); }
            catch (Exception e) { log.error("Błąd auto-zamknięcia: {}", e.getMessage()); }
        }, CLOSING_SIMULATION_SECONDS, TimeUnit.SECONDS);

        return new StatusResponse(currentState, msg);
    }

    private synchronized void autoClose() {
        if (currentState == SystemState.WARNING) {
            String msg = "Rogatki zamknięte automatycznie.";
            transition(SystemState.CLOSED, msg);
        }
    }

    public synchronized StatusResponse handleTrainPassed() {
        if (currentState != SystemState.CLOSED) {
            throw new InvalidStateTransitionException(currentState, SystemState.OPEN);
        }
        String msg = "Pociąg przejechał. Rogatki otworzą się za " + OPENING_SIMULATION_SECONDS + "s.";
        log.info(msg);

        scheduler.schedule(() -> {
            try { autoOpen(); }
            catch (Exception e) { log.error("Błąd auto-otwarcia: {}", e.getMessage()); }
        }, OPENING_SIMULATION_SECONDS, TimeUnit.SECONDS);

        return new StatusResponse(currentState, msg);
    }

    private synchronized void autoOpen() {
        if (currentState == SystemState.CLOSED) {
            String msg = "Rogatki otwarte automatycznie.";
            transition(SystemState.OPEN, msg);
        }
    }

    public synchronized StatusResponse handleObstacle() {
        String msg = "ALARM: Wykryto przeszkodę na torach!";
        transition(SystemState.EMERGENCY, msg);
        throw new ObstacleDetectedException(msg);
    }

    public synchronized StatusResponse handleEmergencyStop() {
        String msg = "Aktywowano zatrzymanie awaryjne.";
        transition(SystemState.EMERGENCY, msg);
        return new StatusResponse(currentState, msg);
    }

    public synchronized StatusResponse handleHardwareFailure() {
        String msg = "AWARIA SPRZĘTU: Brak sygnału zamknięcia rogatek!";
        transition(SystemState.ERROR, msg);
        throw new HardwareFailureException(msg);
    }

    public synchronized StatusResponse emergencyOpen() {
        String msg = "Otwarcie awaryjne wykonane przez operatora.";
        transition(SystemState.OPEN, msg);
        return new StatusResponse(currentState, msg);
    }

    public synchronized StatusResponse getSystemStatus() {
        return new StatusResponse(currentState,
                "Status systemu OK. Dozwolone przejścia: " + currentState.getAllowedTransitions());
    }

    public List<CrossingEvent> getHistory() {
        return eventRepository.findAllByOrderByTimestampDesc();
    }
}