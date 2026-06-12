package projekt.crossing.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum SystemState {
    OPEN,       // Rogatki otwarte, brak zagrożenia
    WARNING,    // Sygnalizacja aktywna, rogatki w trakcie zamykania
    CLOSED,     // Rogatki zamknięte
    EMERGENCY,  // Tryb awaryjny
    ERROR;      // Błąd systemu / utrata zasilania

    private static final Map<SystemState, Set<SystemState>> ALLOWED_TRANSITIONS =
            new EnumMap<>(SystemState.class);

    static {
        ALLOWED_TRANSITIONS.put(OPEN,      EnumSet.of(WARNING, EMERGENCY, ERROR));
        ALLOWED_TRANSITIONS.put(WARNING,   EnumSet.of(CLOSED, OPEN, EMERGENCY, ERROR));
        ALLOWED_TRANSITIONS.put(CLOSED,    EnumSet.of(OPEN, EMERGENCY, ERROR));
        ALLOWED_TRANSITIONS.put(EMERGENCY, EnumSet.of(OPEN, ERROR));
        ALLOWED_TRANSITIONS.put(ERROR,     EnumSet.of(OPEN));
    }

    public boolean canTransitionTo(SystemState target) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(this, EnumSet.noneOf(SystemState.class))
                .contains(target);
    }

    public Set<SystemState> getAllowedTransitions() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(SystemState.class));
    }
}