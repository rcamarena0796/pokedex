package com.pokedex.pokemon.domain.model;

import com.pokedex.pokemon.domain.exception.DomainValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A Pokedex number or a Pokemon name ("25", "pikachu", "mr-mime").
 * Validating it here also prevents arbitrary input from reaching the external API URL.
 */
public record PokemonIdentifier(String value) {

    private static final Pattern VALID = Pattern.compile("^[a-z0-9][a-z0-9-]{0,49}$");

    public PokemonIdentifier {
        value = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new DomainValidationException("idOrName",
                    "Must be a Pokedex number or a name (letters, digits and hyphens)");
        }
        if (value.chars().allMatch(Character::isDigit)
                && (value.length() > 6 || Integer.parseInt(value) < 1)) {
            throw new DomainValidationException("idOrName", "Pokedex number must be between 1 and 999999");
        }
    }

    public static PokemonIdentifier of(String value) {
        return new PokemonIdentifier(value);
    }

    public static PokemonIdentifier of(int id) {
        return new PokemonIdentifier(String.valueOf(id));
    }
}
