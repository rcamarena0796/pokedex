package com.pokedex.pokemon.infrastructure.persistence;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.domain.model.CustomFields;
import com.pokedex.pokemon.domain.model.PageQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs against H2 (PostgreSQL mode, `h2` profile) with the real Flyway migrations, including the 10-Pokemon seed snapshot. */
@DataJpaTest
@ActiveProfiles("h2")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaLocalPokemonRepositoryAdapter.class)
class JpaLocalPokemonRepositoryAdapterTest {

    @Autowired
    private JpaLocalPokemonRepositoryAdapter repository;

    @Test
    void shouldLoadSeededPokemonWithCustomFields() {
        var pikachu = repository.findById(25).orElseThrow();

        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.types()).containsExactly("electric");
        assertThat(pikachu.stats()).hasSize(6);
        assertThat(pikachu.customFields().region()).isEqualTo("Kanto");
        assertThat(pikachu.customFields().tags()).contains("mascot");
    }

    @Test
    void shouldPaginateSortedById() {
        var page = repository.findAll(new PageQuery(0, 5), null);

        assertThat(page.totalItems()).isEqualTo(10);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.items()).extracting("id").containsExactly(1, 2, 3, 4, 7);
    }

    @Test
    void shouldSearchByNameOrLocalizedName() {
        assertThat(repository.findAll(new PageQuery(0, 10), "PIKA").items())
                .extracting("name").containsExactly("pikachu");
        assertThat(repository.findAll(new PageQuery(0, 10), "fushigi").items())
                .extracting("name").containsExactly("bulbasaur", "ivysaur", "venusaur");
    }

    @Test
    void shouldInsertUpdateAndDelete() {
        var charizard = TestData.local(6, "charizard");

        var inserted = repository.save(charizard);
        assertThat(repository.existsById(6)).isTrue();
        assertThat(inserted.stats()).containsExactlyInAnyOrderElementsOf(charizard.stats());
        assertThat(inserted.abilities()).containsExactly("blaze", "solar-power");

        var customized = inserted.customize(new CustomFields("Lizardon", "Kanto", List.of("fire"), null),
                TestData.T0.plusSeconds(60));
        repository.save(customized);
        assertThat(repository.findById(6).orElseThrow().customFields().localizedName()).isEqualTo("Lizardon");

        repository.deleteById(6);
        assertThat(repository.findById(6)).isEmpty();
    }
}
