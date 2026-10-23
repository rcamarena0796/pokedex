package com.pokedex.pokemon.infrastructure.config;

import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.application.service.CatalogService;
import com.pokedex.pokemon.application.service.LocalPokemonService;
import com.pokedex.pokemon.application.service.SyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition root: framework-free application services are created here
 * and receive their ports' implementations (Dependency Inversion).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CatalogService catalogService(PokemonCatalog catalog) {
        return new CatalogService(catalog);
    }

    @Bean
    SyncService syncService(PokemonCatalog catalog, LocalPokemonRepository repository, Clock clock) {
        return new SyncService(catalog, repository, clock);
    }

    @Bean
    LocalPokemonService localPokemonService(LocalPokemonRepository repository, Clock clock) {
        return new LocalPokemonService(repository, clock);
    }
}
