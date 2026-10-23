package com.pokedex.pokemon.application.port.out;

import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;

import java.util.Optional;

public interface LocalPokemonRepository {

    Optional<LocalPokemon> findById(int id);

    PageResult<LocalPokemon> findAll(PageQuery query, String search);

    boolean existsById(int id);

    LocalPokemon save(LocalPokemon pokemon);

    void deleteById(int id);
}
