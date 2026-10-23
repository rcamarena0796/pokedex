package com.pokedex.pokemon.application.port.in;

import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;

import java.util.List;
import java.util.Map;

/** User Story 03: replicate catalog entries into the local database. */
public interface SyncPokemonUseCase {

    int MAX_BATCH = 50;

    SyncResult sync(PokemonIdentifier identifier);

    BatchSyncResult syncRange(int fromId, int toId);

    record SyncResult(LocalPokemon pokemon, boolean created) {
    }

    record BatchSyncResult(List<Integer> created, List<Integer> updated, Map<Integer, String> failed) {
    }
}
