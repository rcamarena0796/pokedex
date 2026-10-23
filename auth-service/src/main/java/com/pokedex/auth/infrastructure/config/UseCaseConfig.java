package com.pokedex.auth.infrastructure.config;

import com.pokedex.auth.application.port.out.PasswordHasher;
import com.pokedex.auth.application.port.out.TokenProvider;
import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.application.service.AuthService;
import com.pokedex.auth.application.service.UserManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition root: the application layer has no Spring annotations,
 * so its services are instantiated here and injected with adapters (Dependency Inversion).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AuthService authService(UserRepository users, PasswordHasher hasher, TokenProvider tokens, Clock clock) {
        return new AuthService(users, hasher, tokens, clock);
    }

    @Bean
    UserManagementService userManagementService(UserRepository users) {
        return new UserManagementService(users);
    }
}
