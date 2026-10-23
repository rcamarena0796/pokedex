package com.pokedex.auth.domain.model;

import com.pokedex.auth.domain.exception.DomainValidationException;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User aggregate. Pure domain object: no framework annotations.
 * All invariants (username/email format) are enforced here so every entry point gets them.
 */
public record User(Long id, String username, String email, String passwordHash, Role role, Instant createdAt) {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public User {
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(passwordHash, "passwordHash is required");
    }

    /** Factory for brand-new users: normalises input and validates invariants. */
    public static User register(String username, String email, String passwordHash, Instant now) {
        var normalizedUsername = normalizeUsername(username);
        var normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        if (!USERNAME.matcher(normalizedUsername).matches()) {
            throw new DomainValidationException("username",
                    "Username must be 3-30 characters: lowercase letters, digits or underscore");
        }
        if (normalizedEmail.length() > 120 || !EMAIL.matcher(normalizedEmail).matches()) {
            throw new DomainValidationException("email", "Email address is not valid");
        }
        return new User(null, normalizedUsername, normalizedEmail, passwordHash, Role.USER, now);
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    public User withRole(Role newRole) {
        return new User(id, username, email, passwordHash, newRole, createdAt);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
