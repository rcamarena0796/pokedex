package com.pokedex.pokemon.infrastructure.pokeapi;

import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.domain.exception.CatalogUnavailableException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.domain.model.PokemonSummary;
import com.pokedex.pokemon.infrastructure.pokeapi.dto.PokeApiDtos;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Implements the PokemonCatalog port on top of PokeAPI.
 * A page needs the Pokemon and its species (for the category), so entries are fetched in parallel.
 */
public class PokeApiCatalogAdapter implements PokemonCatalog {

    private final PokeApiClient client;
    private final Executor executor;

    public PokeApiCatalogAdapter(PokeApiClient client, Executor executor) {
        this.client = client;
        this.executor = executor;
    }

    @Override
    public PageResult<PokemonSummary> findPage(PageQuery query) {
        var list = client.getPokemonList(query.offset(), query.size());

        List<CompletableFuture<PokemonSummary>> futures = list.results().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> summaryOf(entry), executor))
                .toList();

        var items = futures.stream().map(PokeApiCatalogAdapter::join).toList();
        return new PageResult<>(items, query.page(), query.size(), list.count());
    }

    @Override
    public PokemonDetail findDetail(PokemonIdentifier identifier) {
        var pokemon = client.getPokemon(identifier.value());
        var species = client.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        var chain = species.evolutionChain() == null
                ? null
                : client.getEvolutionChain(PokeApiMapper.idFromUrl(species.evolutionChain().url()));
        return PokeApiMapper.toDetail(pokemon, species, chain);
    }

    private PokemonSummary summaryOf(PokeApiDtos.NamedResource entry) {
        var pokemon = client.getPokemon(String.valueOf(PokeApiMapper.idFromUrl(entry.url())));
        var species = client.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        return PokeApiMapper.toSummary(pokemon, species);
    }

    /** Unwraps async failures so callers see the original domain exception. */
    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new CatalogUnavailableException("Failed to load Pokemon from PokeAPI", ex);
        }
    }
}
