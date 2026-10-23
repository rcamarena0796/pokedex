package com.pokedex.auth.domain.exception;

public class OperationNotAllowedException extends DomainException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
