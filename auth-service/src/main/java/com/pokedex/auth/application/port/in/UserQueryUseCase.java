package com.pokedex.auth.application.port.in;

import com.pokedex.auth.domain.model.User;

import java.util.List;

public interface UserQueryUseCase {

    List<User> listUsers();

    User getById(Long id);

    User getByUsername(String username);
}
