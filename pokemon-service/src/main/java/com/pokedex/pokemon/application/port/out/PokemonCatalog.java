package com.pokedex.pokemon.application.port.out;

import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.domain.model.PokemonSummary;

/**
 * Outbound port to the external source of truth. Implemented by the PokeAPI adapter;
 * the application never knows about HTTP, JSON or caching.
 */
public interface PokemonCatalog {

    PageResult<PokemonSummary> findPage(PageQuery query);

    /** @throws com.pokedex.pokemon.domain.exception.PokemonNotFoundException when it does not exist */
    PokemonDetail findDetail(PokemonIdentifier identifier);
}
