package com.pokedex.pokemon.application.port.in;

import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;

/** User Story 02: full detail of one Pokemon from the external catalog. */
public interface GetCatalogPokemonDetailUseCase {

    PokemonDetail getDetail(PokemonIdentifier identifier);
}
