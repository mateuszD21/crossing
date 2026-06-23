package projekt.crossing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.exception.HardwareFailureException;
import projekt.crossing.exception.InvalidStateTransitionException;
import projekt.crossing.exception.ObstacleDetectedException;
import projekt.crossing.model.CrossingEvent;
import projekt.crossing.model.SystemState;
import projekt.crossing.repository.CrossingEventRepository;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CrossingService {

    private static final Logger log = LoggerFactory.getLogger(CrossingService.class);

    // Czasy symulacji (sekundy)
    private static final int CLOSING_SIMULATION_SECONDS = 5;
    private static final int OPENING_SIMULATION_SECONDS = 5;

    private SystemState currentState = SystemState.OPEN;

    private final CrossingEventRepository eventRepository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CrossingService(CrossingEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // -------------------------------------------------------------------------
    // Automat stanów - centralna metoda przejścia (Detka)
    // Ochrona przed SQL Injection: zapis do bazy odbywa się wyłącznie przez
    // JPA/Hibernate z użyciem prepared statements - parametry nigdy nie są
    // wstrzykiwane bezpośrednio do zapytań SQL.
    // -------------------------------------------------------------------------

    private synchronized void transition(SystemState target, String message) {
        if (!currentState.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(currentState, target);
        }
        SystemState previous = currentState;
        currentState = target;
        // Zapis przez JPA - automatyczna ochrona przed SQL Injection
        eventRepository.save(new CrossingEvent(previous, target, message));
        log.info("Zmiana stanu: {} -> {} | {}", previous, target, message);
    }

    // -------------------------------------------------------------------------
    // Operacje publiczne
    // -------------------------------------------------------------------------

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

    public synchronized StatusResponse resetFromEmergency() {
        String msg = "Tryb awaryjny zakończony. System przywrócony do OPEN przez operatora.";
        transition(SystemState.OPEN, msg);
        return new StatusResponse(currentState, msg);
    }

    public synchronized StatusResponse resetFromError() {
        String msg = "System zresetowany z trybu ERROR do OPEN przez operatora.";
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