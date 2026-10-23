package com.pokedex.pokemon.infrastructure.persistence;

import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Converts between the domain LocalPokemon and its JPA row (stats are stored as one column each). */
final class PokemonEntityMapper {

    static final String HP = "hp";
    static final String ATTACK = "attack";
    static final String DEFENSE = "defense";
    static final String SPECIAL_ATTACK = "special-attack";
    static final String SPECIAL_DEFENSE = "special-defense";
    static final String SPEED = "speed";

    private PokemonEntityMapper() {
    }

    static LocalPokemon toDomain(PokemonEntity e) {
        var stats = new ArrayList<Stat>();
        addStat(stats, HP, e.getHp());
        addStat(stats, ATTACK, e.getAttack());
        addStat(stats, DEFENSE, e.getDefense());
        addStat(stats, SPECIAL_ATTACK, e.getSpecialAttack());
        addStat(stats, SPECIAL_DEFENSE, e.getSpecialDefense());
        addStat(stats, SPEED, e.getSpeed());

        var custom = new CustomFields(e.getLocalizedName(), e.getRegion(), e.getTags(), e.getNotes());
        return new LocalPokemon(e.getId(), e.getName(), e.getCategory(), e.getDescription(),
                orZero(e.getHeightM()), orZero(e.getWeightKg()), orEmpty(e.getTypes()), orEmpty(e.getAbilities()),
                stats, e.getSpriteUrl(), e.getImageUrl(), custom, e.getSyncedAt(), e.getUpdatedAt());
    }

    static void copyInto(LocalPokemon p, PokemonEntity e) {
        Map<String, Integer> stats = p.stats().stream()
                .collect(Collectors.toMap(Stat::name, Stat::baseValue, (a, b) -> a));
        e.setId(p.id());
        e.setName(p.name());
        e.setCategory(p.category());
        e.setDescription(p.description());
        e.setHeightM(p.heightM());
        e.setWeightKg(p.weightKg());
        e.setTypes(p.types());
        e.setAbilities(p.abilities());
        e.setHp(stats.get(HP));
        e.setAttack(stats.get(ATTACK));
        e.setDefense(stats.get(DEFENSE));
        e.setSpecialAttack(stats.get(SPECIAL_ATTACK));
        e.setSpecialDefense(stats.get(SPECIAL_DEFENSE));
        e.setSpeed(stats.get(SPEED));
        e.setSpriteUrl(p.spriteUrl());
        e.setImageUrl(p.imageUrl());
        e.setLocalizedName(p.customFields().localizedName());
        e.setRegion(p.customFields().region());
        e.setTags(p.customFields().tags());
        e.setNotes(p.customFields().notes());
        e.setSyncedAt(p.syncedAt());
        e.setUpdatedAt(p.updatedAt());
    }

    private static void addStat(List<Stat> stats, String name, Integer value) {
        if (value != null) {
            stats.add(new Stat(name, value));
        }
    }

    private static double orZero(Double value) {
        return value == null ? 0 : value;
    }

    private static List<String> orEmpty(List<String> values) {
        return values == null ? List.of() : values;
    }
}
