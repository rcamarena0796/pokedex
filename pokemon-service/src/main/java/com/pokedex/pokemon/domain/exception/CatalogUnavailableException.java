package com.pokedex.pokemon.domain.exception;

/** The external Pokemon catalog (PokeAPI) could not be reached or answered with an error. */
public class CatalogUnavailableException extends DomainException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
