package com.pokedex.pokemon.domain.exception;

public class DomainValidationException extends DomainException {

    private final String field;

    public DomainValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
