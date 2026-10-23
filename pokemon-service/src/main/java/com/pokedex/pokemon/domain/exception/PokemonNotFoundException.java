package com.pokedex.pokemon.domain.exception;

public class PokemonNotFoundException extends DomainException {

    public PokemonNotFoundException(Object identifier) {
        super("Pokemon '%s' was not found".formatted(identifier));
    }
}
