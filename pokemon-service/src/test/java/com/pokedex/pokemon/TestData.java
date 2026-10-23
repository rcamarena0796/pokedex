package com.pokedex.pokemon;

import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.EvolutionNode;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonSummary;
import com.pokedex.pokemon.domain.model.Stat;

import java.time.Instant;
import java.util.List;

/** Small builders shared by unit and integration tests. */
public final class TestData {

    public static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private TestData() {
    }

    public static PokemonDetail detail(int id, String name) {
        var chain = new EvolutionNode(id, name, "https://img/" + id + ".png", List.of());
        return new PokemonDetail(id, name, "https://img/" + id + ".png", "https://sprite/" + id + ".png",
                "Flame Pokémon", "Spits fire that is hot enough to melt boulders.", 1.7, 90.5,
                List.of("fire", "flying"), List.of("blaze", "solar-power"),
                List.of(new Stat("hp", 78), new Stat("attack", 84), new Stat("defense", 78),
                        new Stat("special-attack", 109), new Stat("special-defense", 85), new Stat("speed", 100)),
                chain);
    }

    public static PokemonSummary summary(int id, String name) {
        return new PokemonSummary(id, name, "https://sprite/" + id + ".png", "Seed Pokémon", 6.9,
                List.of("grass"), List.of("overgrow"));
    }

    public static LocalPokemon local(int id, String name) {
        return LocalPokemon.fromCatalog(detail(id, name), T0);
    }

    public static LocalPokemon customized(int id, String name) {
        return local(id, name).customize(
                new CustomFields("リザードン (Lizardon)", "Kanto", List.of("starter-line"), "notes"), T0);
    }
}
