package com.pokedex.auth.infrastructure.config;

import com.pokedex.auth.application.port.out.PasswordHasher;
import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Seeds demo credentials on startup (idempotent). Passwords are hashed at runtime,
 * so no hashes are committed to the repository. Disable with app.seed.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Clock clock;

    public DemoUserSeeder(UserRepository users, PasswordHasher hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("admin", "admin@pokedex.local", "admin123", Role.ADMIN);
        seed("ash", "ash@pokedex.local", "pikachu123", Role.USER);
    }

    private void seed(String username, String email, String password, Role role) {
        if (users.existsByUsername(username)) {
            return;
        }
        var user = User.register(username, email, hasher.hash(password), clock.instant()).withRole(role);
        users.save(user);
        log.info("Seeded demo user '{}' with role {}", username, role);
    }
}
