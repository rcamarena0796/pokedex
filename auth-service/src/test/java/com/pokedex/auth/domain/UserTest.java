package com.pokedex.auth.domain;

import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldNormaliseAndDefaultToUserRole() {
        var user = User.register("  Misty_01 ", " Misty@Cerulean.GYM ", "hash", NOW);

        assertThat(user.username()).isEqualTo("misty_01");
        assertThat(user.email()).isEqualTo("misty@cerulean.gym");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(user.id()).isNull();
        assertThat(user.createdAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "has space", "way_too_long_username_over_thirty_chars", "bad-dash"})
    void shouldRejectInvalidUsernames(String username) {
        assertThatThrownBy(() -> User.register(username, "a@b.co", "hash", NOW))
                .isInstanceOf(DomainValidationException.class)
                .extracting("field").isEqualTo("username");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"no-at-sign", "a@b", "two@@signs.com", "spaces @x.com"})
    void shouldRejectInvalidEmails(String email) {
        assertThatThrownBy(() -> User.register("brock", email, "hash", NOW))
                .isInstanceOf(DomainValidationException.class)
                .extracting("field").isEqualTo("email");
    }

    @Test
    void shouldChangeRoleImmutably() {
        var user = User.register("brock", "brock@pewter.gym", "hash", NOW);

        var admin = user.withRole(Role.ADMIN);

        assertThat(admin.isAdmin()).isTrue();
        assertThat(user.isAdmin()).isFalse();
    }
}
