package projekt.crossing.service;

import org.springframework.stereotype.Service;
import projekt.crossing.exception.HardwareFailureException;
import projekt.crossing.model.SystemState;
import projekt.crossing.dto.StatusResponse;
import projekt.crossing.exception.ObstacleDetectedException;

@Service
public class CrossingService {

    private SystemState currentState = SystemState.OPEN;

    public StatusResponse handleTrainApproach() {
        if (currentState == SystemState.OPEN) {
            currentState = SystemState.WARNING;
            return new StatusResponse(currentState, "Wykryto pociąg. Uruchomiono sygnalizację świetlną. Rozpoczynam procedurę.");
        }
        return new StatusResponse(currentState, "Procedura zamknięcia jest już w toku lub system jest zablokowany.");
    }

    public StatusResponse handleObstacle() {
        currentState = SystemState.EMERGENCY;
        //  wyjątek
        throw new ObstacleDetectedException("ALARM: Wykryto auto na torach! Procedura zamykania wstrzymana. Poinformowano dyżurnego.");
    }

    public StatusResponse handleEmergencyStop() {
        currentState = SystemState.EMERGENCY;
        return new StatusResponse(currentState, "SUKCES: Aktywowano zatrzymanie awaryjne. Powiadomiono technika.");
    }

    public StatusResponse getSystemStatus() {
        return new StatusResponse(currentState, "Wszystkie podsystemy raportują status OK.");
    }
    public StatusResponse handleHardwareFailure() {
        currentState = SystemState.ERROR;
        throw new HardwareFailureException("AWARIA SPRZĘTU: Brak sygnału potwierdzającego zamknięcie rogatek! Powiadomiono technika.");
    }
}