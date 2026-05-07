package projekt.crossing.model;

public enum SystemState {
    OPEN,       // Rogatki otwarte, brak zagrożenia
    WARNING,    // Sygnalizacja aktywna, rogatki w trakcie zamykania
    CLOSED,     // Rogatki zamknięte
    EMERGENCY,  // Tryb awaryjny
    ERROR       // Błąd systemu / utrata zasilania
}