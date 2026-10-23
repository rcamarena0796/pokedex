package com.pokedex.pokemon.domain;

import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.model.CustomFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomFieldsTest {

    @Test
    void shouldNormaliseValues() {
        var fields = new CustomFields("  ピカチュウ ", " Kanto ", Arrays.asList(" Mascot", "mascot", "FAN-favorite "), "   ");

        assertThat(fields.localizedName()).isEqualTo("ピカチュウ");
        assertThat(fields.region()).isEqualTo("Kanto");
        assertThat(fields.tags()).containsExactly("mascot", "fan-favorite");
        assertThat(fields.notes()).isNull();
    }

    @Test
    void shouldTreatNullTagsAsEmpty() {
        assertThat(new CustomFields(null, null, null, null).tags()).isEmpty();
        assertThat(CustomFields.empty().tags()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"has space", "under_score", "-leading-hyphen", "this-tag-is-way-too-long-for-us-1", ""})
    void shouldRejectInvalidTags(String tag) {
        assertThatThrownBy(() -> new CustomFields(null, null, List.of(tag), null))
                .isInstanceOf(DomainValidationException.class)
                .extracting("field").isEqualTo("tags");
    }

    @Test
    void shouldRejectMoreThanTenDistinctTags() {
        var tags = IntStream.rangeClosed(1, 11).mapToObj(i -> "tag" + i).toList();

        assertThatThrownBy(() -> new CustomFields(null, null, tags, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldCountTagsAfterDeduplication() {
        var tags = IntStream.rangeClosed(1, 12).mapToObj(i -> i % 2 == 0 ? "same" : "SAME").toList();

        assertThat(new CustomFields(null, null, tags, null).tags()).containsExactly("same");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Kanto 2", "Region<script>", "Sevii Islands Sevii Islands Sevii Islands X"})
    void shouldRejectInvalidRegion(String region) {
        assertThatThrownBy(() -> new CustomFields(null, region, null, null))
                .isInstanceOf(DomainValidationException.class)
                .extracting("field").isEqualTo("region");
    }

    @Test
    void shouldAcceptRegionsWithAccentsAndPunctuation() {
        assertThat(new CustomFields(null, "Île-de-France St. John's", null, null).region())
                .isEqualTo("Île-de-France St. John's");
    }

    @Test
    void shouldRejectOverlongTexts() {
        assertThatThrownBy(() -> new CustomFields("x".repeat(61), null, null, null))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new CustomFields(null, null, null, "x".repeat(501)))
                .isInstanceOf(DomainValidationException.class);
    }
}
