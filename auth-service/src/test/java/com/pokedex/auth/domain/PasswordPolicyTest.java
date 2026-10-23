package com.pokedex.auth.domain;

import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.model.PasswordPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void shouldAcceptPasswordWithLettersAndDigits() {
        assertThatCode(() -> PasswordPolicy.validate("pikachu123")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"short1", "onlyletters", "1234567890"})
    void shouldRejectWeakPasswords(String password) {
        assertThatThrownBy(() -> PasswordPolicy.validate(password))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRejectPasswordsLongerThanBcryptLimit() {
        assertThatThrownBy(() -> PasswordPolicy.validate("a1".repeat(37)))
                .isInstanceOf(DomainValidationException.class);
    }
}
