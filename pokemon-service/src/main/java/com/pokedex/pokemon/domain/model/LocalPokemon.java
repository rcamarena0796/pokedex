package com.pokedex.pokemon.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A Pokemon replicated into our own database (User Story 03).
 * Catalog data comes from PokeAPI; {@link CustomFields} are ours and survive re-synchronisation.
 */
public record LocalPokemon(
        int id,
        String name,
        String category,
        String description,
        double heightM,
        double weightKg,
        List<String> types,
        List<String> abilities,
        List<Stat> stats,
        String spriteUrl,
        String imageUrl,
        CustomFields customFields,
        Instant syncedAt,
        Instant updatedAt) {

    public LocalPokemon {
        Objects.requireNonNull(name, "name is required");
        types = List.copyOf(types);
        abilities = List.copyOf(abilities);
        stats = List.copyOf(stats);
        customFields = customFields == null ? CustomFields.empty() : customFields;
    }

    /** First synchronisation: copy catalog data, no custom fields yet. */
    public static LocalPokemon fromCatalog(PokemonDetail detail, Instant now) {
        return new LocalPokemon(detail.id(), detail.name(), detail.category(), detail.description(),
                detail.heightM(), detail.weightKg(), detail.types(), detail.abilities(), detail.stats(),
                detail.spriteUrl(), detail.imageUrl(), CustomFields.empty(), now, now);
    }

    /** Re-synchronisation: refresh catalog data while keeping our proprietary fields. */
    public LocalPokemon refreshFrom(PokemonDetail detail, Instant now) {
        return new LocalPokemon(id, detail.name(), detail.category(), detail.description(),
                detail.heightM(), detail.weightKg(), detail.types(), detail.abilities(), detail.stats(),
                detail.spriteUrl(), detail.imageUrl(), customFields, now, now);
    }

    /** User Story 04: replace the proprietary fields. */
    public LocalPokemon customize(CustomFields newFields, Instant now) {
        return new LocalPokemon(id, name, category, description, heightM, weightKg, types, abilities, stats,
                spriteUrl, imageUrl, newFields, syncedAt, now);
    }
}
