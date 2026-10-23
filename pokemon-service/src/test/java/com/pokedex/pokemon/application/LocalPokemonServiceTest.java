package com.pokedex.pokemon.application;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.application.port.in.LocalPokemonCommandUseCase.UpdateCustomFieldsCommand;
import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.application.service.LocalPokemonService;
import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LocalPokemonServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-01T09:00:00Z");

    private LocalPokemonRepository repository;
    private LocalPokemonService service;

    @BeforeEach
    void setUp() {
        repository = mock(LocalPokemonRepository.class);
        service = new LocalPokemonService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldListWithNormalisedSearch() {
        var query = new PageQuery(0, 10);
        var page = new PageResult<>(List.of(TestData.local(6, "charizard")), 0, 10, 1);
        when(repository.findAll(query, null)).thenReturn(page);
        when(repository.findAll(query, "char")).thenReturn(page);

        assertThat(service.list(query, "   ")).isEqualTo(page);
        assertThat(service.list(query, " char ")).isEqualTo(page);
        verify(repository).findAll(query, null);
    }

    @Test
    void shouldRejectOverlongSearch() {
        assertThatThrownBy(() -> service.list(new PageQuery(0, 10), "x".repeat(51)))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldGetStoredPokemon() {
        var charizard = TestData.local(6, "charizard");
        when(repository.findById(6)).thenReturn(Optional.of(charizard));

        assertThat(service.get(6)).isEqualTo(charizard);
    }

    @Test
    void shouldThrowNotFoundForMissingPokemon() {
        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(404)).isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void shouldRejectNonPositiveId() {
        assertThatThrownBy(() -> service.get(0)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> service.delete(-3)).isInstanceOf(DomainValidationException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldUpdateCustomFields() {
        when(repository.findById(6)).thenReturn(Optional.of(TestData.local(6, "charizard")));

        var updated = service.updateCustomFields(6,
                new UpdateCustomFieldsCommand(" Dracaufeu ", "Kanto", List.of("Starter-Line", "fire"), "Big wings"));

        assertThat(updated.customFields().localizedName()).isEqualTo("Dracaufeu");
        assertThat(updated.customFields().tags()).containsExactly("starter-line", "fire");
        assertThat(updated.updatedAt()).isEqualTo(NOW);
        verify(repository).save(updated);
    }

    @Test
    void shouldValidateBeforeLoading() {
        var invalid = new UpdateCustomFieldsCommand(null, "R3gion", null, null);

        assertThatThrownBy(() -> service.updateCustomFields(6, invalid))
                .isInstanceOf(DomainValidationException.class);
        verify(repository, never()).findById(anyInt());
    }

    @Test
    void shouldRejectMissingCommand() {
        assertThatThrownBy(() -> service.updateCustomFields(6, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingPokemon() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCustomFields(999, new UpdateCustomFieldsCommand(null, null, null, null)))
                .isInstanceOf(PokemonNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteStoredPokemon() {
        when(repository.existsById(6)).thenReturn(true);

        service.delete(6);

        verify(repository).deleteById(6);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingPokemon() {
        when(repository.existsById(6)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(6)).isInstanceOf(PokemonNotFoundException.class);
        verify(repository, never()).deleteById(anyInt());
    }
}
