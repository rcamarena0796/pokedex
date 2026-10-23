package com.pokedex.auth.infrastructure.web;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void adminShouldListUsers() {
        given().auth().oauth2(adminToken())
                .get("/users")
                .then().statusCode(200)
                .body("username", hasItems("admin", "ash"));
    }

    @Test
    void regularUserShouldBeForbidden() {
        given().auth().oauth2(tokenFor("ash", "pikachu123"))
                .get("/users")
                .then().statusCode(403);
    }

    @Test
    void shouldReturn404ForMissingUser() {
        given().auth().oauth2(adminToken())
                .get("/users/99999")
                .then().statusCode(404);
    }

    @Test
    void shouldReturn400ForNonNumericOrNegativeId() {
        var token = adminToken();
        given().auth().oauth2(token).get("/users/abc").then().statusCode(400);
        given().auth().oauth2(token).get("/users/-1").then().statusCode(400);
    }

    @Test
    void adminShouldPromoteAndDeleteUser() {
        Integer id = given().contentType(ContentType.JSON)
                .body(Map.of("username", "brock", "email", "brock@pewter.gym", "password", "onix12345"))
                .post("/auth/register")
                .then().statusCode(201)
                .extract().path("id");
        var token = adminToken();

        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("role", "ADMIN"))
                .put("/users/" + id + "/role")
                .then().statusCode(200)
                .body("role", equalTo("ADMIN"));

        given().auth().oauth2(token).delete("/users/" + id).then().statusCode(204);
        given().auth().oauth2(token).get("/users/" + id).then().statusCode(404);
    }

    @Test
    void shouldRejectUnknownRole() {
        given().auth().oauth2(adminToken()).contentType(ContentType.JSON)
                .body(Map.of("role", "SUPERUSER"))
                .put("/users/2/role")
                .then().statusCode(400);
    }

    @Test
    void adminShouldNotDeleteThemselves() {
        Integer adminId = given().auth().oauth2(adminToken()).get("/auth/me").then().extract().path("id");

        given().auth().oauth2(adminToken())
                .delete("/users/" + adminId)
                .then().statusCode(409);
    }
}
