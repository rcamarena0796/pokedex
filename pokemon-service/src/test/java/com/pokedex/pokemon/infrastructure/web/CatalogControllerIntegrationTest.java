package com.pokedex.pokemon.infrastructure.web;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.domain.exception.CatalogUnavailableException;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CatalogControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldBrowseCatalogWithoutAuthentication() {
        when(catalog.findPage(new PageQuery(0, 2))).thenReturn(new PageResult<>(
                List.of(TestData.summary(1, "bulbasaur"), TestData.summary(2, "ivysaur")), 0, 2, 1302));

        given().queryParam("page", 0).queryParam("size", 2)
                .get("/catalog/pokemon")
                .then().statusCode(200)
                .header("Cache-Control", containsString("max-age=600"))
                .body("items.size()", equalTo(2))
                .body("items[0].name", equalTo("bulbasaur"))
                .body("items[0].category", equalTo("Seed Pokémon"))
                .body("items[0].weightKg", equalTo(6.9f))
                .body("items[0].abilities[0]", equalTo("overgrow"))
                .body("totalItems", equalTo(1302))
                .body("totalPages", equalTo(651));
    }

    @Test
    void shouldRejectInvalidPagination() {
        given().queryParam("size", 0).get("/catalog/pokemon").then().statusCode(400);
        given().queryParam("size", 51).get("/catalog/pokemon").then().statusCode(400);
        given().queryParam("page", -1).get("/catalog/pokemon").then().statusCode(400);
        given().queryParam("page", "abc").get("/catalog/pokemon").then().statusCode(400);
        verifyNoInteractions(catalog);
    }

    @Test
    void shouldReturnDetail() {
        when(catalog.findDetail(PokemonIdentifier.of("charizard"))).thenReturn(TestData.detail(6, "charizard"));

        given().get("/catalog/pokemon/Charizard")
                .then().statusCode(200)
                .body("id", equalTo(6))
                .body("stats.size()", equalTo(6))
                .body("description", containsString("melt boulders"))
                .body("evolutionChain.name", equalTo("charizard"));
    }

    @Test
    void shouldReturn404ForUnknownPokemon() {
        when(catalog.findDetail(any())).thenThrow(new PokemonNotFoundException("missingno"));

        given().get("/catalog/pokemon/missingno")
                .then().statusCode(404)
                .body("detail", containsString("missingno"));
    }

    @Test
    void shouldReturn400ForInvalidIdentifier() {
        given().get("/catalog/pokemon/bad_name").then().statusCode(400).body("errors.idOrName", equalTo(
                "Must be a Pokedex number or a name (letters, digits and hyphens)"));
        verifyNoInteractions(catalog);
    }

    @Test
    void shouldReturn502WhenPokeApiIsDown() {
        when(catalog.findDetail(any())).thenThrow(new CatalogUnavailableException("PokeAPI is unavailable", null));

        given().get("/catalog/pokemon/pikachu").then().statusCode(502);
    }
}
