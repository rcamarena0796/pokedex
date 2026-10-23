package com.pokedex.pokemon.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** The second identical request must be served from the cache, not from PokeAPI. */
@SpringJUnitConfig(PokeApiClientCachingTest.Config.class)
class PokeApiClientCachingTest {

    private static final String BASE = "https://pokeapi.test/api/v2";

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder().baseUrl(BASE);
        }

        @Bean
        MockRestServiceServer server(RestClient.Builder builder) {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean
        PokeApiClient pokeApiClient(RestClient.Builder builder, MockRestServiceServer server) {
            // "server" is a parameter so the mock is bound to the builder before the client is built.
            return new PokeApiClient(builder.build());
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private PokeApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldCallPokeApiOnlyOnceForTheSamePokemon() {
        server.expect(ExpectedCount.once(), requestTo(BASE + "/pokemon/1"))
                .andRespond(withSuccess(new ClassPathResource("pokeapi/pokemon-1.json"), MediaType.APPLICATION_JSON));

        var first = client.getPokemon("1");
        var second = client.getPokemon("1");

        assertThat(second).isEqualTo(first);
        server.verify();
    }
}
