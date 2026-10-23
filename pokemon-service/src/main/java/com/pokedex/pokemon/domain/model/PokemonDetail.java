package com.pokedex.pokemon.domain.model;

import java.util.List;

/** User Story 02: everything needed for the detail view. */
public record PokemonDetail(
        int id,
        String name,
        String imageUrl,
        String spriteUrl,
        String category,
        String description,
        double heightM,
        double weightKg,
        List<String> types,
        List<String> abilities,
        List<Stat> stats,
        EvolutionNode evolutionChain) {

    public PokemonDetail {
        types = List.copyOf(types);
        abilities = List.copyOf(abilities);
        stats = List.copyOf(stats);
    }
}
