package com.pokedex.pokemon.domain;

import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void identifierShouldNormalise() {
        assertThat(PokemonIdentifier.of("  Mr-Mime ").value()).isEqualTo("mr-mime");
        assertThat(PokemonIdentifier.of(25).value()).isEqualTo("25");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "1234567", "bad_name", "../etc/passwd", "pika chu", "-pikachu"})
    void identifierShouldRejectInvalidValues(String value) {
        assertThatThrownBy(() -> PokemonIdentifier.of(value)).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void pageQueryShouldComputeOffset() {
        assertThat(new PageQuery(3, 20).offset()).isEqualTo(60);
    }

    @ParameterizedTest
    @CsvSource({"-1,20", "0,0", "0,51"})
    void pageQueryShouldRejectInvalidValues(int page, int size) {
        assertThatThrownBy(() -> new PageQuery(page, size)).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void pageResultShouldComputeTotalPagesAndMap() {
        var result = new PageResult<>(List.of(1, 2), 0, 2, 5);

        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.map(i -> "#" + i).items()).containsExactly("#1", "#2");
    }
}
