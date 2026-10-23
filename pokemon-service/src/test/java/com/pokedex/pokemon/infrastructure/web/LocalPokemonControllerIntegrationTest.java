package com.pokedex.pokemon.infrastructure.web;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LocalPokemonControllerIntegrationTest extends AbstractIntegrationTest {

    // --- authentication ----------------------------------------------------------------------

    @Test
    void shouldRequireToken() {
        given().get("/pokemon").then().statusCode(401).body("status", equalTo(401));
    }

    @Test
    void shouldRejectExpiredOrForeignTokens() {
        // Well past the decoder's default 60s clock-skew allowance.
        var expired = token("ash", "USER", "https://auth.pokedex.local", Instant.now().minusSeconds(600));
        var foreign = token("ash", "USER", "someone-else", Instant.now().plusSeconds(600));

        given().auth().oauth2(expired).get("/pokemon").then().statusCode(401);
        given().auth().oauth2(foreign).get("/pokemon").then().statusCode(401);
        given().auth().oauth2("not-a-jwt").get("/pokemon").then().statusCode(401);
    }

    // --- read -------------------------------------------------------------------------------

    @Test
    void shouldListSeededPokemon() {
        given().auth().oauth2(userToken())
                .get("/pokemon")
                .then().statusCode(200)
                .body("items.name", hasItems("bulbasaur", "pikachu", "mewtwo"))
                .body("page", equalTo(0))
                .body("size", equalTo(20));
    }

    @Test
    void shouldSearchByLocalizedName() {
        given().auth().oauth2(userToken()).queryParam("search", "kabigon")
                .get("/pokemon")
                .then().statusCode(200)
                .body("totalItems", equalTo(1))
                .body("items[0].name", equalTo("snorlax"));
    }

    @Test
    void shouldGetOneWithCustomFields() {
        given().auth().oauth2(userToken())
                .get("/pokemon/25")
                .then().statusCode(200)
                .body("name", equalTo("pikachu"))
                .body("region", equalTo("Kanto"))
                .body("tags", hasItems("mascot", "electric"))
                .body("stats.size()", equalTo(6));
    }

    @Test
    void shouldValidateIdOnRead() {
        var token = userToken();
        given().auth().oauth2(token).get("/pokemon/9999").then().statusCode(404);
        given().auth().oauth2(token).get("/pokemon/0").then().statusCode(400);
        given().auth().oauth2(token).get("/pokemon/abc").then().statusCode(400);
    }

    // --- update (User Story 04) ---------------------------------------------------------------

    @Test
    void shouldUpdateCustomFields() {
        given().auth().oauth2(userToken()).contentType(ContentType.JSON)
                .body(Map.of("localizedName", "Évoli", "region", "Kalos",
                        "tags", List.of(" Fan-Favorite ", "evolution", "EVOLUTION"), "notes", "French name"))
                .put("/pokemon/133")
                .then().statusCode(200)
                .body("localizedName", equalTo("Évoli"))
                .body("region", equalTo("Kalos"))
                .body("tags", equalTo(List.of("fan-favorite", "evolution")))
                .body("name", equalTo("eevee"));
    }

    @Test
    void shouldReturn404WhenUpdatingMissingPokemon() {
        given().auth().oauth2(userToken()).contentType(ContentType.JSON)
                .body(Map.of("region", "Johto"))
                .put("/pokemon/9999")
                .then().statusCode(404);
    }

    @Test
    void shouldReturn400ForInvalidPayloads() {
        var token = userToken();
        var elevenTags = IntStream.rangeClosed(1, 11).mapToObj(i -> "t" + i).toList();

        // bean validation
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("tags", elevenTags))
                .put("/pokemon/25").then().statusCode(400).body("errors.tags", notNullValue());
        // domain validation
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("tags", List.of("no spaces allowed")))
                .put("/pokemon/25").then().statusCode(400).body("errors.tags", containsString("invalid"));
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("region", "R3gion"))
                .put("/pokemon/25").then().statusCode(400).body("errors.region", notNullValue());
        // unknown property
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("color", "yellow"))
                .put("/pokemon/25").then().statusCode(400);
        // malformed JSON
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("{\"region\": ")
                .put("/pokemon/25").then().statusCode(400);
        // wrong JSON type
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body("{\"tags\": \"not-a-list\"}")
                .put("/pokemon/25").then().statusCode(400);
    }

    @Test
    void shouldRequireTokenToUpdate() {
        given().contentType(ContentType.JSON).body(Map.of("region", "Kanto"))
                .put("/pokemon/25").then().statusCode(401);
    }

    // --- sync (User Story 03) and delete -------------------------------------------------------

    @Test
    void shouldSyncThenResyncThenDelete() {
        when(catalog.findDetail(PokemonIdentifier.of("6"))).thenReturn(TestData.detail(6, "charizard"));

        given().auth().oauth2(userToken())
                .post("/pokemon/sync/6")
                .then().statusCode(201)
                .header("Location", endsWith("/pokemon/6"))
                .body("name", equalTo("charizard"));

        given().auth().oauth2(userToken())
                .post("/pokemon/sync/6")
                .then().statusCode(200);

        given().auth().oauth2(userToken()).delete("/pokemon/6").then().statusCode(403);
        given().auth().oauth2(adminToken()).delete("/pokemon/6").then().statusCode(204);
        given().auth().oauth2(adminToken()).get("/pokemon/6").then().statusCode(404);
        given().auth().oauth2(adminToken()).delete("/pokemon/6").then().statusCode(404);
    }

    @Test
    void shouldReturn404WhenSyncingUnknownPokemon() {
        when(catalog.findDetail(any())).thenThrow(new PokemonNotFoundException("missingno"));

        given().auth().oauth2(userToken()).post("/pokemon/sync/missingno").then().statusCode(404);
    }

    @Test
    void shouldSyncRange() {
        when(catalog.findDetail(PokemonIdentifier.of(151))).thenReturn(TestData.detail(151, "mew"));
        when(catalog.findDetail(PokemonIdentifier.of(152))).thenThrow(new PokemonNotFoundException(152));

        given().auth().oauth2(userToken()).queryParam("fromId", 151).queryParam("toId", 152)
                .post("/pokemon/sync")
                .then().statusCode(200)
                .body("created", equalTo(List.of(151)))
                .body("failed.'152'", containsString("not found"));
    }

    @Test
    void shouldRejectInvalidSyncRange() {
        var token = userToken();
        given().auth().oauth2(token).queryParam("fromId", 10).queryParam("toId", 5)
                .post("/pokemon/sync").then().statusCode(400);
        given().auth().oauth2(token).queryParam("fromId", 1).queryParam("toId", 100)
                .post("/pokemon/sync").then().statusCode(400);
        given().auth().oauth2(token).queryParam("fromId", 1)
                .post("/pokemon/sync").then().statusCode(400);
    }
}
