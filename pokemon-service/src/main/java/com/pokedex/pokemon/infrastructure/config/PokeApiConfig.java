package com.pokedex.pokemon.infrastructure.config;

import com.pokedex.pokemon.infrastructure.pokeapi.PokeApiCatalogAdapter;
import com.pokedex.pokemon.infrastructure.pokeapi.PokeApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executors;

/**
 * Wires the PokeAPI adapter. Caching (Caffeine) is configured in application.yaml.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(PokeApiProperties.class)
public class PokeApiConfig {

    @Bean
    PokeApiClient pokeApiClient(RestClient.Builder builder, PokeApiProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());

        var restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "pokedex-interview-exercise")
                .build();
        return new PokeApiClient(restClient);
    }

    /**
     * Virtual threads give a cheap parallel fan-out of the per-Pokemon calls a catalog page needs.
     * The executor is deliberately not a bean, so it does not replace Spring Boot's default task executor.
     */
    @Bean
    PokeApiCatalogAdapter pokemonCatalog(PokeApiClient client) {
        return new PokeApiCatalogAdapter(client, Executors.newVirtualThreadPerTaskExecutor());
    }
}
