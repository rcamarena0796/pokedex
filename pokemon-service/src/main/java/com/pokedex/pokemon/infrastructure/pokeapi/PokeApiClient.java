package com.pokedex.pokemon.infrastructure.pokeapi;

import com.pokedex.pokemon.domain.exception.CatalogUnavailableException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.infrastructure.pokeapi.dto.PokeApiDtos;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

/**
 * Thin HTTP client for PokeAPI. Each raw resource is cached individually, so the same
 * Pokemon/species fetched for the list, the detail view or a sync is downloaded only once.
 */
public class PokeApiClient {

    public static final String LIST_CACHE = "pokeapi-list";
    public static final String POKEMON_CACHE = "pokeapi-pokemon";
    public static final String SPECIES_CACHE = "pokeapi-species";
    public static final String EVOLUTION_CACHE = "pokeapi-evolution";

    private final RestClient restClient;

    public PokeApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(cacheNames = LIST_CACHE, key = "#offset + ':' + #limit")
    public PokeApiDtos.ResourceList getPokemonList(int offset, int limit) {
        return call("pokemon list", () -> restClient.get()
                .uri(uri -> uri.path("/pokemon").queryParam("offset", offset).queryParam("limit", limit).build())
                .retrieve()
                .body(PokeApiDtos.ResourceList.class));
    }

    @Cacheable(cacheNames = POKEMON_CACHE)
    public PokeApiDtos.Pokemon getPokemon(String idOrName) {
        return call(idOrName, () -> restClient.get().uri("/pokemon/{idOrName}", idOrName)
                .retrieve().body(PokeApiDtos.Pokemon.class));
    }

    @Cacheable(cacheNames = SPECIES_CACHE)
    public PokeApiDtos.Species getSpecies(int speciesId) {
        return call(speciesId, () -> restClient.get().uri("/pokemon-species/{id}", speciesId)
                .retrieve().body(PokeApiDtos.Species.class));
    }

    @Cacheable(cacheNames = EVOLUTION_CACHE)
    public PokeApiDtos.EvolutionChain getEvolutionChain(int chainId) {
        return call(chainId, () -> restClient.get().uri("/evolution-chain/{id}", chainId)
                .retrieve().body(PokeApiDtos.EvolutionChain.class));
    }

    /** Translates transport failures into domain exceptions so nothing HTTP-specific leaks out. */
    private static <T> T call(Object identifier, Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new CatalogUnavailableException("PokeAPI returned an empty response", null);
            }
            return body;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                throw new PokemonNotFoundException(identifier);
            }
            throw new CatalogUnavailableException("PokeAPI rejected the request: " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new CatalogUnavailableException("PokeAPI is unavailable, please try again later", ex);
        }
    }
}
