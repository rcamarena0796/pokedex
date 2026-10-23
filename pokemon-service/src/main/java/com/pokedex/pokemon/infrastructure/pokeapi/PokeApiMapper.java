package com.pokedex.pokemon.infrastructure.pokeapi;

import com.pokedex.pokemon.domain.model.EvolutionNode;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonSummary;
import com.pokedex.pokemon.domain.model.Stat;
import com.pokedex.pokemon.infrastructure.pokeapi.dto.PokeApiDtos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Converts PokeAPI resources into domain objects (anti-corruption layer). */
final class PokeApiMapper {

    static final String ARTWORK_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";
    private static final String ENGLISH = "en";

    private PokeApiMapper() {
    }

    static PokemonSummary toSummary(PokeApiDtos.Pokemon pokemon, PokeApiDtos.Species species) {
        return new PokemonSummary(pokemon.id(), pokemon.name(), spriteUrl(pokemon), category(species),
                hectogramsToKg(pokemon.weight()), types(pokemon), abilities(pokemon));
    }

    static PokemonDetail toDetail(PokeApiDtos.Pokemon pokemon, PokeApiDtos.Species species,
                                  PokeApiDtos.EvolutionChain chain) {
        return new PokemonDetail(pokemon.id(), pokemon.name(), imageUrl(pokemon), spriteUrl(pokemon),
                category(species), description(species),
                decimetresToMetres(pokemon.height()), hectogramsToKg(pokemon.weight()),
                types(pokemon), abilities(pokemon), stats(pokemon), chain == null ? null : toNode(chain.chain()));
    }

    /** "Category" in the Pokedex is the species genus, e.g. "Seed Pokémon". */
    static String category(PokeApiDtos.Species species) {
        return nullSafe(species.genera()).stream()
                .filter(g -> g.language() != null && ENGLISH.equals(g.language().name()))
                .map(PokeApiDtos.Genus::genus)
                .findFirst()
                .orElse(null);
    }

    /** Latest English Pokedex entry, with the form-feed/newline artefacts from the games removed. */
    static String description(PokeApiDtos.Species species) {
        var entries = nullSafe(species.flavorTextEntries()).stream()
                .filter(f -> f.language() != null && ENGLISH.equals(f.language().name()))
                .map(PokeApiDtos.FlavorText::flavorText)
                .filter(Objects::nonNull)
                .toList();
        if (entries.isEmpty()) {
            return null;
        }
        return entries.getLast()
                .replaceAll("\\s+", " ")
                .replace("POKéMON", "Pokémon")
                .trim();
    }

    static int idFromUrl(String url) {
        // e.g. https://pokeapi.co/api/v2/pokemon-species/25/
        var trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private static EvolutionNode toNode(PokeApiDtos.ChainLink link) {
        int speciesId = idFromUrl(link.species().url());
        var children = nullSafe(link.evolvesTo()).stream().map(PokeApiMapper::toNode).toList();
        return new EvolutionNode(speciesId, link.species().name(), ARTWORK_URL.formatted(speciesId), children);
    }

    private static String spriteUrl(PokeApiDtos.Pokemon pokemon) {
        return pokemon.sprites() == null ? null : pokemon.sprites().frontDefault();
    }

    private static String imageUrl(PokeApiDtos.Pokemon pokemon) {
        var sprites = pokemon.sprites();
        if (sprites != null && sprites.other() != null && sprites.other().officialArtwork() != null
                && sprites.other().officialArtwork().frontDefault() != null) {
            return sprites.other().officialArtwork().frontDefault();
        }
        return spriteUrl(pokemon);
    }

    private static List<String> types(PokeApiDtos.Pokemon pokemon) {
        return nullSafe(pokemon.types()).stream()
                .sorted(Comparator.comparingInt(PokeApiDtos.TypeSlot::slot))
                .map(t -> t.type().name())
                .toList();
    }

    private static List<String> abilities(PokeApiDtos.Pokemon pokemon) {
        return nullSafe(pokemon.abilities()).stream()
                .sorted(Comparator.comparingInt(PokeApiDtos.AbilitySlot::slot))
                .map(a -> a.ability().name())
                .toList();
    }

    private static List<Stat> stats(PokeApiDtos.Pokemon pokemon) {
        return nullSafe(pokemon.stats()).stream()
                .map(s -> new Stat(s.stat().name(), s.baseStat()))
                .toList();
    }

    private static double hectogramsToKg(int hectograms) {
        return hectograms / 10.0;
    }

    private static double decimetresToMetres(int decimetres) {
        return decimetres / 10.0;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
