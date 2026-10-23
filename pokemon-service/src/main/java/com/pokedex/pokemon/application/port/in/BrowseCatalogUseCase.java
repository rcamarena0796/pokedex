package com.pokedex.pokemon.application.port.in;

import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonSummary;

/** User Story 01: browse the external catalog page by page. */
public interface BrowseCatalogUseCase {

    PageResult<PokemonSummary> browse(PageQuery query);
}
