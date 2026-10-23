package com.pokedex.pokemon.application.service;

import com.pokedex.pokemon.application.port.in.LocalPokemonCommandUseCase;
import com.pokedex.pokemon.application.port.in.LocalPokemonQueryUseCase;
import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;

import java.time.Clock;

public class LocalPokemonService implements LocalPokemonQueryUseCase, LocalPokemonCommandUseCase {

    private final LocalPokemonRepository repository;
    private final Clock clock;

    public LocalPokemonService(LocalPokemonRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public PageResult<LocalPokemon> list(PageQuery query, String search) {
        var normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        if (normalizedSearch != null && normalizedSearch.length() > 50) {
            throw new DomainValidationException("search", "Search must be at most 50 characters");
        }
        return repository.findAll(query, normalizedSearch);
    }

    @Override
    public LocalPokemon get(int id) {
        return repository.findById(requireValidId(id)).orElseThrow(() -> new PokemonNotFoundException(id));
    }

    @Override
    public LocalPokemon updateCustomFields(int id, UpdateCustomFieldsCommand command) {
        if (command == null) {
            throw new DomainValidationException("body", "Request body is required");
        }
        // Build (and validate) the new value before loading, so invalid input never needs a DB round-trip.
        var fields = new CustomFields(command.localizedName(), command.region(), command.tags(), command.notes());
        var current = get(id);
        return repository.save(current.customize(fields, clock.instant()));
    }

    @Override
    public void delete(int id) {
        if (!repository.existsById(requireValidId(id))) {
            throw new PokemonNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private static int requireValidId(int id) {
        if (id < 1) {
            throw new DomainValidationException("id", "Id must be a positive Pokedex number");
        }
        return id;
    }
}
