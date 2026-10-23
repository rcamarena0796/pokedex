package com.pokedex.pokemon.application.port.in;

import com.pokedex.pokemon.domain.model.LocalPokemon;

import java.util.List;

/** User Story 04: modify or remove locally stored Pokemon. */
public interface LocalPokemonCommandUseCase {

    LocalPokemon updateCustomFields(int id, UpdateCustomFieldsCommand command);

    void delete(int id);

    record UpdateCustomFieldsCommand(String localizedName, String region, List<String> tags, String notes) {
    }
}
