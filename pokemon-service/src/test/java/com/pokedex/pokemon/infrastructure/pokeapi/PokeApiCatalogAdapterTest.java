package com.pokedex.pokemon.infrastructure.pokeapi;

import com.pokedex.pokemon.domain.exception.CatalogUnavailableException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Verifies the anti-corruption layer against JSON shaped like real PokeAPI responses. */
class PokeApiCatalogAdapterTest {

    private static final String BASE = "https://pokeapi.test/api/v2";

    private MockRestServiceServer server;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        // Direct executor keeps the test deterministic (no threads).
        adapter = new PokeApiCatalogAdapter(new PokeApiClient(builder.build()), Runnable::run);
    }

    @Test
    void shouldMapCatalogPage() {
        respond("/pokemon?offset=0&limit=2", "pokemon-list.json");
        respond("/pokemon/1", "pokemon-1.json");
        respond("/pokemon/2", "pokemon-2.json");
        respond("/pokemon-species/1", "species-1.json");
        respond("/pokemon-species/2", "species-2.json");

        var page = adapter.findPage(new PageQuery(0, 2));

        assertThat(page.totalItems()).isEqualTo(1302);
        assertThat(page.totalPages()).isEqualTo(651);
        assertThat(page.items()).hasSize(2);
        var bulbasaur = page.items().getFirst();
        assertThat(bulbasaur.id()).isEqualTo(1);
        assertThat(bulbasaur.name()).isEqualTo("bulbasaur");
        assertThat(bulbasaur.category()).isEqualTo("Seed Pokémon");
        assertThat(bulbasaur.weightKg()).isEqualTo(6.9);
        assertThat(bulbasaur.types()).containsExactly("grass", "poison");
        assertThat(bulbasaur.abilities()).containsExactly("overgrow", "chlorophyll");
        assertThat(bulbasaur.spriteUrl()).endsWith("/sprites/pokemon/1.png");
        server.verify();
    }

    @Test
    void shouldMapDetailWithDescriptionStatsAndEvolutionChain() {
        respond("/pokemon/bulbasaur", "pokemon-1.json");
        respond("/pokemon-species/1", "species-1.json");
        respond("/evolution-chain/1", "evolution-chain-1.json");

        var detail = adapter.findDetail(PokemonIdentifier.of("bulbasaur"));

        assertThat(detail.imageUrl()).endsWith("/official-artwork/1.png");
        assertThat(detail.heightM()).isEqualTo(0.7);
        assertThat(detail.description())
                .isEqualTo("While it is young, it uses the nutrients that are stored in the seed on its back in order to grow.");
        assertThat(detail.stats()).hasSize(6).contains(new Stat("hp", 45), new Stat("special-attack", 65));

        var chain = detail.evolutionChain();
        assertThat(chain.name()).isEqualTo("bulbasaur");
        assertThat(chain.evolvesTo().getFirst().name()).isEqualTo("ivysaur");
        var venusaur = chain.evolvesTo().getFirst().evolvesTo().getFirst();
        assertThat(venusaur.speciesId()).isEqualTo(3);
        assertThat(venusaur.imageUrl()).endsWith("/official-artwork/3.png");
        assertThat(venusaur.evolvesTo()).isEmpty();
    }

    @Test
    void shouldTranslate404ToNotFound() {
        server.expect(requestTo(BASE + "/pokemon/missingno")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> adapter.findDetail(PokemonIdentifier.of("missingno")))
                .isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void shouldTranslateServerErrorToCatalogUnavailable() {
        server.expect(requestTo(BASE + "/pokemon/pikachu")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.findDetail(PokemonIdentifier.of("pikachu")))
                .isInstanceOf(CatalogUnavailableException.class);
    }

    @Test
    void shouldExtractIdFromResourceUrl() {
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/25/")).isEqualTo(25);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/evolution-chain/10")).isEqualTo(10);
    }

    private void respond(String path, String fixture) {
        server.expect(requestTo(BASE + path))
                .andRespond(withSuccess(new ClassPathResource("pokeapi/" + fixture), MediaType.APPLICATION_JSON));
    }
}
