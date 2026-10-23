package com.pokedex.auth.infrastructure.persistence;

import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adapter implementing the application's UserRepository port with Spring Data JPA. */
@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public JpaUserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(JpaUserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll(Sort.by("id")).stream().map(JpaUserRepositoryAdapter::toDomain).toList();
    }

    @Override
    public long countByRole(Role role) {
        return jpa.countByRole(role);
    }

    @Override
    public User save(User user) {
        return toDomain(jpa.save(toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getUsername(), e.getEmail(), e.getPasswordHash(), e.getRole(), e.getCreatedAt());
    }

    static UserEntity toEntity(User u) {
        return new UserEntity(u.id(), u.username(), u.email(), u.passwordHash(), u.role(), u.createdAt());
    }
}
