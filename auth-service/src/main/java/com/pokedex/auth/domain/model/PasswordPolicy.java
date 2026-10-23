package com.pokedex.auth.domain.model;

import com.pokedex.auth.domain.exception.DomainValidationException;

/**
 * Password rules live in the domain, independent of how the password is hashed.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 72; // BCrypt only uses the first 72 bytes

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH || rawPassword.length() > MAX_LENGTH) {
            throw new DomainValidationException("password",
                    "Password must be between %d and %d characters".formatted(MIN_LENGTH, MAX_LENGTH));
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new DomainValidationException("password", "Password must contain at least one letter and one digit");
        }
    }
}
