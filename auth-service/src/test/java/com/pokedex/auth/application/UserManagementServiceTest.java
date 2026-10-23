package com.pokedex.auth.application;

import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.application.service.UserManagementService;
import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.exception.OperationNotAllowedException;
import com.pokedex.auth.domain.exception.UserNotFoundException;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserManagementServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final User ADMIN = new User(1L, "admin", "admin@x.io", "h", Role.ADMIN, NOW);
    private static final User ASH = new User(2L, "ash", "ash@x.io", "h", Role.USER, NOW);

    private UserRepository users;
    private UserManagementService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        service = new UserManagementService(users);
        when(users.findById(1L)).thenReturn(Optional.of(ADMIN));
        when(users.findById(2L)).thenReturn(Optional.of(ASH));
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldListUsers() {
        when(users.findAll()).thenReturn(List.of(ADMIN, ASH));

        assertThat(service.listUsers()).containsExactly(ADMIN, ASH);
    }

    @Test
    void shouldThrowWhenUserIsMissing() {
        when(users.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldFindByNormalisedUsername() {
        when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));

        assertThat(service.getByUsername(" Ash ")).isEqualTo(ASH);
    }

    @Test
    void shouldPromoteUser() {
        var promoted = service.changeRole(2L, Role.ADMIN, "admin");

        assertThat(promoted.role()).isEqualTo(Role.ADMIN);
        verify(users).save(promoted);
    }

    @Test
    void shouldRequireRole() {
        assertThatThrownBy(() -> service.changeRole(2L, null, "admin"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldNotAllowChangingOwnRole() {
        assertThatThrownBy(() -> service.changeRole(1L, Role.USER, "admin"))
                .isInstanceOf(OperationNotAllowedException.class);
    }

    @Test
    void shouldNotDemoteLastAdmin() {
        when(users.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.changeRole(1L, Role.USER, "other_admin"))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(users, never()).save(any());
    }

    @Test
    void shouldDeleteUser() {
        service.deleteUser(2L, "admin");

        verify(users).deleteById(2L);
    }

    @Test
    void shouldNotDeleteSelf() {
        assertThatThrownBy(() -> service.deleteUser(1L, "admin"))
                .isInstanceOf(OperationNotAllowedException.class);
        verify(users, never()).deleteById(anyLong());
    }

    @Test
    void shouldNotDeleteLastAdmin() {
        when(users.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteUser(1L, "other_admin"))
                .isInstanceOf(OperationNotAllowedException.class);
    }
}
