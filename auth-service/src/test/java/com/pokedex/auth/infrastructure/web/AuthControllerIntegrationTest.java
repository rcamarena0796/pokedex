package com.pokedex.auth.infrastructure.web;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldRegisterThenLoginThenReadProfile() {
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "Misty", "email", "misty@cerulean.gym", "password", "starmie123"))
                .post("/auth/register")
                .then().statusCode(201)
                .header("Location", notNullValue())
                .body("username", equalTo("misty"))
                .body("role", equalTo("USER"))
                .body("$", not(hasKey("passwordHash")));

        var token = tokenFor("misty", "starmie123");

        given().auth().oauth2(token)
                .get("/auth/me")
                .then().statusCode(200)
                .body("username", equalTo("misty"))
                .body("email", equalTo("misty@cerulean.gym"));
    }

    @Test
    void shouldReturn400WithFieldErrorsForInvalidPayload() {
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "x", "email", "not-an-email", "password", "123"))
                .post("/auth/register")
                .then().statusCode(400)
                .body("errors.username", notNullValue())
                .body("errors.email", notNullValue())
                .body("errors.password", notNullValue());
    }

    @Test
    void shouldReturn400ForMalformedJson() {
        given().contentType(ContentType.JSON)
                .body("{\"username\": ")
                .post("/auth/register")
                .then().statusCode(400);
    }

    @Test
    void shouldReturn409ForDuplicateUsername() {
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "ash", "email", "another@kanto.com", "password", "pikachu123"))
                .post("/auth/register")
                .then().statusCode(409);
    }

    @Test
    void shouldReturn401ForBadCredentials() {
        given().contentType(ContentType.JSON)
                .body(Map.of("username", "ash", "password", "wrong-password1"))
                .post("/auth/login")
                .then().statusCode(401)
                .body("detail", equalTo("Invalid username or password"));
    }

    @Test
    void shouldReturn401ForProfileWithoutToken() {
        given().get("/auth/me").then().statusCode(401).body("status", equalTo(401));
    }
}
