package com.pokedex.auth.application;

import com.pokedex.auth.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.pokedex.auth.application.port.out.PasswordHasher;
import com.pokedex.auth.application.port.out.TokenProvider;
import com.pokedex.auth.application.port.out.TokenProvider.IssuedToken;
import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.application.service.AuthService;
import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.exception.InvalidCredentialsException;
import com.pokedex.auth.domain.exception.UserAlreadyExistsException;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private UserRepository users;
    private PasswordHasher hasher;
    private TokenProvider tokens;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        hasher = mock(PasswordHasher.class);
        tokens = mock(TokenProvider.class);
        service = new AuthService(users, hasher, tokens, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldRegisterUserWithHashedPassword() {
        when(hasher.hash("pikachu123")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 7L));

        var user = service.register(new RegisterUserCommand("Ash", "ash@kanto.com", "pikachu123"));

        assertThat(user.id()).isEqualTo(7L);
        assertThat(user.username()).isEqualTo("ash");
        assertThat(user.passwordHash()).isEqualTo("hashed");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(user.createdAt()).isEqualTo(NOW);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(hasher.hash(any())).thenReturn("hashed");
        when(users.existsByUsername("ash")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("ash", "ash@kanto.com", "pikachu123")))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(users, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(hasher.hash(any())).thenReturn("hashed");
        when(users.existsByEmail("ash@kanto.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("ash", "ASH@kanto.com", "pikachu123")))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(users, never()).save(any());
    }

    @Test
    void shouldRejectWeakPasswordBeforeTouchingDependencies() {
        assertThatThrownBy(() -> service.register(new RegisterUserCommand("ash", "ash@kanto.com", "weak")))
                .isInstanceOf(DomainValidationException.class);
        verifyNoInteractions(users, hasher);
    }

    @Test
    void shouldIssueTokenForValidCredentials() {
        var ash = new User(1L, "ash", "ash@kanto.com", "hashed", Role.USER, NOW);
        when(users.findByUsername("ash")).thenReturn(Optional.of(ash));
        when(hasher.matches("pikachu123", "hashed")).thenReturn(true);
        when(tokens.issue(ash)).thenReturn(new IssuedToken("jwt", NOW.plusSeconds(60)));

        var result = service.authenticate(" ASH ", "pikachu123");

        assertThat(result.accessToken()).isEqualTo("jwt");
        assertThat(result.expiresAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(result.user()).isEqualTo(ash);
    }

    @Test
    void shouldRejectWrongPassword() {
        var ash = new User(1L, "ash", "ash@kanto.com", "hashed", Role.USER, NOW);
        when(users.findByUsername("ash")).thenReturn(Optional.of(ash));
        when(hasher.matches("nope", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate("ash", "nope"))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(tokens);
    }

    @Test
    void shouldRejectUnknownUserWithSameErrorAsWrongPassword() {
        when(users.findByUsername("gary")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("gary", "whatever1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private static User withId(User user, Long id) {
        return new User(id, user.username(), user.email(), user.passwordHash(), user.role(), user.createdAt());
    }
}
