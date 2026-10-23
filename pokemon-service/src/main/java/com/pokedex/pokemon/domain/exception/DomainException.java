package com.pokedex.pokemon.domain.exception;

/** Base type for every business-rule failure. The web layer maps subtypes to HTTP statuses. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
