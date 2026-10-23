package com.pokedex.pokemon.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonJpaRepository extends JpaRepository<PokemonEntity, Integer> {

    Page<PokemonEntity> findByNameContainingIgnoreCaseOrLocalizedNameContainingIgnoreCase(
            String name, String localizedName, Pageable pageable);
}
