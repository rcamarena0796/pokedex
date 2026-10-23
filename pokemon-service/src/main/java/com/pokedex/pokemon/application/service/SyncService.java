package com.pokedex.pokemon.application.service;

import com.pokedex.pokemon.application.port.in.SyncPokemonUseCase;
import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.domain.exception.DomainException;
import com.pokedex.pokemon.domain.exception.DomainValidationException;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Pulls Pokemon from the catalog into the local store. Idempotent: syncing an existing
 * Pokemon refreshes its catalog data and keeps its custom fields.
 */
public class SyncService implements SyncPokemonUseCase {

    private final PokemonCatalog catalog;
    private final LocalPokemonRepository repository;
    private final Clock clock;

    public SyncService(PokemonCatalog catalog, LocalPokemonRepository repository, Clock clock) {
        this.catalog = catalog;
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public SyncResult sync(PokemonIdentifier identifier) {
        var detail = catalog.findDetail(identifier);
        var now = clock.instant();

        var existing = repository.findById(detail.id());
        var pokemon = existing
                .map(local -> local.refreshFrom(detail, now))
                .orElseGet(() -> LocalPokemon.fromCatalog(detail, now));

        return new SyncResult(repository.save(pokemon), existing.isEmpty());
    }

    @Override
    public BatchSyncResult syncRange(int fromId, int toId) {
        if (fromId < 1) {
            throw new DomainValidationException("fromId", "fromId must be 1 or greater");
        }
        if (toId < fromId) {
            throw new DomainValidationException("toId", "toId must be greater than or equal to fromId");
        }
        if (toId - fromId + 1 > MAX_BATCH) {
            throw new DomainValidationException("toId", "A batch can contain at most %d Pokemon".formatted(MAX_BATCH));
        }

        var created = new ArrayList<Integer>();
        var updated = new ArrayList<Integer>();
        var failed = new LinkedHashMap<Integer, String>();

        for (int id = fromId; id <= toId; id++) {
            try {
                var result = sync(PokemonIdentifier.of(id));
                (result.created() ? created : updated).add(id);
            } catch (DomainException ex) {
                // One bad entry should not abort the whole batch; report it instead.
                failed.put(id, ex.getMessage());
            }
        }
        return new BatchSyncResult(created, updated, failed);
    }
}
