package com.pokedex.auth.application.port.in;

import com.pokedex.auth.domain.model.User;

public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);

    record RegisterUserCommand(String username, String email, String password) {
    }
}
