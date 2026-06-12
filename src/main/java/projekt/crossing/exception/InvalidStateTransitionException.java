package projekt.crossing.exception;

import projekt.crossing.model.SystemState;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(SystemState from, SystemState to) {
        super("Niedozwolone przejście stanu: " + from + " -> " + to
                + ". Dozwolone przejścia z " + from + ": " + from.getAllowedTransitions());
    }
}