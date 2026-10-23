package com.pokedex.auth.application.port.out;

import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;

import java.util.List;
import java.util.Optional;

/** Outbound port: persistence contract owned by the application layer. */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAll();

    long countByRole(Role role);

    User save(User user);

    void deleteById(Long id);
}
