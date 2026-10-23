package com.pokedex.auth.application.port.out;

import com.pokedex.auth.domain.model.User;

import java.time.Instant;

public interface TokenProvider {

    IssuedToken issue(User user);

    record IssuedToken(String value, Instant expiresAt) {
    }
}
