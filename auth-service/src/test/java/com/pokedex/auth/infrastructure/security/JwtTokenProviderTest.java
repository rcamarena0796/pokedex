package com.pokedex.auth.infrastructure.security;

import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtProperties properties = new JwtProperties(
            "test-secret-test-secret-test-secret-123", "https://auth.pokedex.local", Duration.ofHours(2));
    private final SecurityConfig config = new SecurityConfig();

    @Test
    void shouldIssueTokenThatTheDecoderAccepts() {
        var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var provider = new JwtTokenProvider(config.jwtEncoder(properties), properties, Clock.fixed(now, ZoneOffset.UTC));
        var user = new User(5L, "misty", "misty@x.io", "h", Role.ADMIN, now);

        var token = provider.issue(user);
        var decoded = config.jwtDecoder(properties).decode(token.value());

        assertThat(token.expiresAt()).isEqualTo(now.plus(Duration.ofHours(2)));
        assertThat(decoded.getSubject()).isEqualTo("misty");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("https://auth.pokedex.local");
        assertThat(decoded.getExpiresAt()).isEqualTo(token.expiresAt());
    }
}
