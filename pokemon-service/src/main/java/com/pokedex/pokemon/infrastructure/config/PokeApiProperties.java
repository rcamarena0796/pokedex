package com.pokedex.pokemon.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "pokeapi")
public record PokeApiProperties(@NotBlank String baseUrl, @NotNull Duration connectTimeout,
                                @NotNull Duration readTimeout) {
}
