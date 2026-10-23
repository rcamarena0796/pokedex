package com.pokedex.pokemon.domain.model;

import java.util.List;

/** User Story 01: one entry of the paginated catalog. */
public record PokemonSummary(
        int id,
        String name,
        String spriteUrl,
        String category,
        double weightKg,
        List<String> types,
        List<String> abilities) {

    public PokemonSummary {
        types = List.copyOf(types);
        abilities = List.copyOf(abilities);
    }
}
