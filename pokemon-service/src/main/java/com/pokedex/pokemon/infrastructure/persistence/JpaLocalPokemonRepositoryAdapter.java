package com.pokedex.pokemon.infrastructure.persistence;

import com.pokedex.pokemon.application.port.out.LocalPokemonRepository;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Implements the LocalPokemonRepository port with Spring Data JPA. */
@Repository
@Transactional(readOnly = true)
public class JpaLocalPokemonRepositoryAdapter implements LocalPokemonRepository {

    private final PokemonJpaRepository jpa;

    public JpaLocalPokemonRepositoryAdapter(PokemonJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<LocalPokemon> findById(int id) {
        return jpa.findById(id).map(PokemonEntityMapper::toDomain);
    }

    @Override
    public PageResult<LocalPokemon> findAll(PageQuery query, String search) {
        var pageable = PageRequest.of(query.page(), query.size(), Sort.by("id"));
        var page = search == null
                ? jpa.findAll(pageable)
                : jpa.findByNameContainingIgnoreCaseOrLocalizedNameContainingIgnoreCase(search, search, pageable);
        var items = page.getContent().stream().map(PokemonEntityMapper::toDomain).toList();
        return new PageResult<>(items, query.page(), query.size(), page.getTotalElements());
    }

    @Override
    public boolean existsById(int id) {
        return jpa.existsById(id);
    }

    @Override
    @Transactional
    public LocalPokemon save(LocalPokemon pokemon) {
        var entity = jpa.findById(pokemon.id()).orElseGet(PokemonEntity::new);
        PokemonEntityMapper.copyInto(pokemon, entity);
        return PokemonEntityMapper.toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        jpa.deleteById(id);
    }
}
