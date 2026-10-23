package com.pokedex.auth.domain.exception;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String field, String value) {
        super("A user with %s '%s' already exists".formatted(field, value));
    }
}
