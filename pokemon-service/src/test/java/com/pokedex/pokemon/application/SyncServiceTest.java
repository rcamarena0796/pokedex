package com.pokedex.pokemon.application;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.application.service.SyncService;
import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SyncServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-05T12:00:00Z");

    private PokemonCatalog catalog;
    private LocalPokemonRepository repository;
    private SyncService service;

    @BeforeEach
    void setUp() {
        catalog = mock(PokemonCatalog.class);
        repository = mock(LocalPokemonRepository.class);
        service = new SyncService(catalog, repository, Clock.fixed(NOW, ZoneOffset.UTC));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findById(anyInt())).thenReturn(Optional.empty());
    }

    @Test
    void shouldCreateLocalCopyWhenNotStoredYet() {
        when(catalog.findDetail(PokemonIdentifier.of("charizard"))).thenReturn(TestData.detail(6, "charizard"));

        var result = service.sync(PokemonIdentifier.of("charizard"));

        assertThat(result.created()).isTrue();
        assertThat(result.pokemon().id()).isEqualTo(6);
        assertThat(result.pokemon().customFields()).isEqualTo(CustomFields.empty());
        assertThat(result.pokemon().syncedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldRefreshAndKeepCustomFieldsWhenAlreadyStored() {
        var existing = TestData.customized(6, "charizard");
        when(repository.findById(6)).thenReturn(Optional.of(existing));
        when(catalog.findDetail(PokemonIdentifier.of(6))).thenReturn(TestData.detail(6, "charizard"));

        var result = service.sync(PokemonIdentifier.of(6));

        assertThat(result.created()).isFalse();
        assertThat(result.pokemon().customFields()).isEqualTo(existing.customFields());
        assertThat(result.pokemon().syncedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReportPerEntryOutcomeForBatch() {
        when(catalog.findDetail(PokemonIdentifier.of(1))).thenReturn(TestData.detail(1, "bulbasaur"));
        when(catalog.findDetail(PokemonIdentifier.of(2))).thenThrow(new PokemonNotFoundException(2));
        when(catalog.findDetail(PokemonIdentifier.of(3))).thenReturn(TestData.detail(3, "venusaur"));
        when(repository.findById(3)).thenReturn(Optional.of(TestData.local(3, "venusaur")));

        var result = service.syncRange(1, 3);

        assertThat(result.created()).containsExactly(1);
        assertThat(result.updated()).containsExactly(3);
        assertThat(result.failed()).containsOnlyKeys(2);
    }

    @ParameterizedTest
    @CsvSource({"0,5", "10,5", "1,51"})
    void shouldRejectInvalidRanges(int fromId, int toId) {
        assertThatThrownBy(() -> service.syncRange(fromId, toId)).isInstanceOf(DomainValidationException.class);
        verifyNoInteractions(catalog);
    }

    @Test
    void shouldAcceptMaximumBatchSize() {
        when(catalog.findDetail(any())).thenThrow(new PokemonNotFoundException("x"));

        var result = service.syncRange(1, 50);

        assertThat(result.failed()).hasSize(50);
    }
}
