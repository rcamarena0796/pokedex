package com.pokedex.auth.infrastructure.web.dto;

import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;

import java.time.Instant;

/** Public view of a user. Never exposes the password hash. */
public record UserResponse(Long id, String username, String email, Role role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.username(), user.email(), user.role(), user.createdAt());
    }
}
