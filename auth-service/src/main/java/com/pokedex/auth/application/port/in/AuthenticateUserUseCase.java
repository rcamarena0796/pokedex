package com.pokedex.auth.application.port.in;

import com.pokedex.auth.domain.model.User;

import java.time.Instant;

public interface AuthenticateUserUseCase {

    AuthenticationResult authenticate(String username, String password);

    record AuthenticationResult(String accessToken, Instant expiresAt, User user) {
    }
}
