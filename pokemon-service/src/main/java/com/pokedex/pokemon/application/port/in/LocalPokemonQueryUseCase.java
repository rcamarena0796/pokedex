package com.pokedex.pokemon.application.port.in;

import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;

public interface LocalPokemonQueryUseCase {

    /** @param search optional, matches the name or the localized name */
    PageResult<LocalPokemon> list(PageQuery query, String search);

    LocalPokemon get(int id);
}
