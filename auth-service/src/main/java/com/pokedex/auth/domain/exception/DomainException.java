package com.pokedex.auth.domain.exception;

/** Base type for every business-rule failure. The web layer maps subtypes to HTTP statuses. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
