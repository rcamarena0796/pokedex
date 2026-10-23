package com.pokedex.pokemon.infrastructure.web;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Boots the whole service (H2 + Flyway seed) on a random port.
 * PokeAPI is replaced by a mock of the PokemonCatalog port, so tests never hit the network.
 * Tokens are minted with the shared secret, exactly as auth-service would.
 */
@ActiveProfiles("h2")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @MockitoBean
    protected PokemonCatalog catalog;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
    }

    protected String userToken() {
        return token("ash", "USER");
    }

    protected String adminToken() {
        return token("admin", "ADMIN");
    }

    protected String token(String username, String role) {
        return token(username, role, issuer, Instant.now().plus(1, ChronoUnit.HOURS));
    }

    protected String token(String username, String role, String tokenIssuer, Instant expiresAt) {
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var claims = JwtClaimsSet.builder()
                .issuer(tokenIssuer)
                .subject(username)
                .issuedAt(expiresAt.minus(2, ChronoUnit.HOURS))
                .expiresAt(expiresAt)
                .claim("roles", List.of(role))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
