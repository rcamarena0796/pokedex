package com.pokedex.auth.infrastructure.persistence;

import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaUserRepositoryAdapter.class)
class JpaUserRepositoryAdapterTest {

    @Autowired
    private JpaUserRepositoryAdapter repository;

    @Test
    void shouldSaveAndFindUser() {
        var saved = repository.save(User.register("brock", "brock@pewter.gym", "hash",
                Instant.now().truncatedTo(ChronoUnit.MILLIS)));

        assertThat(saved.id()).isNotNull();
        assertThat(repository.findById(saved.id())).contains(saved);
        assertThat(repository.findByUsername("brock")).contains(saved);
        assertThat(repository.existsByUsername("brock")).isTrue();
        assertThat(repository.existsByEmail("brock@pewter.gym")).isTrue();
        assertThat(repository.existsByUsername("gary")).isFalse();
    }

    @Test
    void shouldCountByRoleAndDelete() {
        var admin = repository.save(User.register("erika", "erika@celadon.gym", "hash", Instant.now())
                .withRole(Role.ADMIN));

        assertThat(repository.countByRole(Role.ADMIN)).isEqualTo(1);

        repository.deleteById(admin.id());

        assertThat(repository.findById(admin.id())).isEmpty();
        assertThat(repository.countByRole(Role.ADMIN)).isZero();
    }
}
