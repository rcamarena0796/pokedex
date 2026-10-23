package com.pokedex.pokemon.infrastructure.persistence;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.domain.model.Stat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonEntityMapperTest {

    @Test
    void shouldRoundTripDomainThroughEntity() {
        var pokemon = TestData.customized(6, "charizard");

        var entity = new PokemonEntity();
        PokemonEntityMapper.copyInto(pokemon, entity);

        assertThat(entity.getSpecialAttack()).isEqualTo(109);
        assertThat(entity.getTags()).containsExactly("starter-line");
        assertThat(PokemonEntityMapper.toDomain(entity)).isEqualTo(pokemon);
    }

    @Test
    void shouldDefaultMissingColumnsAndSkipMissingStats() {
        var entity = new PokemonEntity();
        entity.setId(25);
        entity.setName("pikachu");
        entity.setHp(35);

        var pokemon = PokemonEntityMapper.toDomain(entity);

        assertThat(pokemon.heightM()).isZero();
        assertThat(pokemon.weightKg()).isZero();
        assertThat(pokemon.types()).isEmpty();
        assertThat(pokemon.abilities()).isEmpty();
        assertThat(pokemon.stats()).containsExactly(new Stat("hp", 35));
        assertThat(pokemon.customFields().tags()).isEmpty();
    }
}
