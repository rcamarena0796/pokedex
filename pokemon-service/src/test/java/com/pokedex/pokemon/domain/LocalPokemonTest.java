package com.pokedex.pokemon.domain;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.pokedex.pokemon.TestData.T0;
import static org.assertj.core.api.Assertions.assertThat;

class LocalPokemonTest {

    @Test
    void shouldCopyCatalogDataOnFirstSync() {
        var detail = TestData.detail(6, "charizard");

        var local = LocalPokemon.fromCatalog(detail, T0);

        assertThat(local.id()).isEqualTo(6);
        assertThat(local.name()).isEqualTo("charizard");
        assertThat(local.category()).isEqualTo(detail.category());
        assertThat(local.stats()).hasSize(6);
        assertThat(local.customFields()).isEqualTo(CustomFields.empty());
        assertThat(local.syncedAt()).isEqualTo(T0);
        assertThat(local.updatedAt()).isEqualTo(T0);
    }

    @Test
    void shouldKeepCustomFieldsWhenRefreshing() {
        var customized = TestData.customized(6, "charizard");
        var later = T0.plusSeconds(3600);
        var refreshedCatalog = TestData.detail(6, "charizard-renamed");

        var refreshed = customized.refreshFrom(refreshedCatalog, later);

        assertThat(refreshed.name()).isEqualTo("charizard-renamed");
        assertThat(refreshed.customFields()).isEqualTo(customized.customFields());
        assertThat(refreshed.syncedAt()).isEqualTo(later);
    }

    @Test
    void shouldReplaceCustomFieldsWithoutTouchingSyncTime() {
        var local = TestData.local(6, "charizard");
        var later = Instant.parse("2026-02-01T00:00:00Z");
        var fields = new CustomFields("Dracaufeu", "Kanto", List.of("fire"), null);

        var updated = local.customize(fields, later);

        assertThat(updated.customFields()).isEqualTo(fields);
        assertThat(updated.updatedAt()).isEqualTo(later);
        assertThat(updated.syncedAt()).isEqualTo(T0);
        assertThat(updated.name()).isEqualTo("charizard");
    }
}
