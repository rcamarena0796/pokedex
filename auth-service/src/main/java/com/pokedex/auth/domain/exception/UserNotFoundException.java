package com.pokedex.auth.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Object identifier) {
        super("User '%s' was not found".formatted(identifier));
    }
}
