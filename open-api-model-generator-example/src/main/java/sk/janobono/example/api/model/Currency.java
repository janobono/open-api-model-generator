package sk.janobono.example.api.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Currency {

    EURO("€"),
    US_DOLLAR("$");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    @Override
    public String toString() {
        return symbol;
    }
}
