package com.pokedex.pokemon.application.service;

import com.pokedex.pokemon.application.port.in.BrowseCatalogUseCase;
import com.pokedex.pokemon.application.port.in.GetCatalogPokemonDetailUseCase;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.domain.model.PokemonSummary;

import java.util.Objects;

public class CatalogService implements BrowseCatalogUseCase, GetCatalogPokemonDetailUseCase {

    private final PokemonCatalog catalog;

    public CatalogService(PokemonCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public PageResult<PokemonSummary> browse(PageQuery query) {
        return catalog.findPage(Objects.requireNonNull(query, "query is required"));
    }

    @Override
    public PokemonDetail getDetail(PokemonIdentifier identifier) {
        return catalog.findDetail(Objects.requireNonNull(identifier, "identifier is required"));
    }
}
