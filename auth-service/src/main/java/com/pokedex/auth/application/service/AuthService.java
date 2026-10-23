package com.pokedex.auth.application.service;

import com.pokedex.auth.application.port.in.AuthenticateUserUseCase;
import com.pokedex.auth.application.port.in.RegisterUserUseCase;
import com.pokedex.auth.application.port.out.PasswordHasher;
import com.pokedex.auth.application.port.out.TokenProvider;
import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.domain.exception.InvalidCredentialsException;
import com.pokedex.auth.domain.exception.UserAlreadyExistsException;
import com.pokedex.auth.domain.model.PasswordPolicy;
import com.pokedex.auth.domain.model.User;

import java.time.Clock;

/**
 * Registration and login use cases. Depends only on ports, so it is framework-free
 * and wired as a bean in the infrastructure configuration.
 */
public class AuthService implements RegisterUserUseCase, AuthenticateUserUseCase {

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final Clock clock;

    public AuthService(UserRepository users, PasswordHasher passwordHasher, TokenProvider tokenProvider, Clock clock) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.clock = clock;
    }

    @Override
    public User register(RegisterUserCommand command) {
        PasswordPolicy.validate(command.password());
        // The factory validates and normalises username/email, so uniqueness is checked on canonical values.
        var user = User.register(command.username(), command.email(),
                passwordHasher.hash(command.password()), clock.instant());

        if (users.existsByUsername(user.username())) {
            throw new UserAlreadyExistsException("username", user.username());
        }
        if (users.existsByEmail(user.email())) {
            throw new UserAlreadyExistsException("email", user.email());
        }
        return users.save(user);
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        var user = users.findByUsername(User.normalizeUsername(username))
                .filter(found -> password != null && passwordHasher.matches(password, found.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        var token = tokenProvider.issue(user);
        return new AuthenticationResult(token.value(), token.expiresAt(), user);
    }
}
