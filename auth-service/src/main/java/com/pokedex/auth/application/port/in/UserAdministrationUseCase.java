package com.pokedex.auth.application.port.in;

import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;

public interface UserAdministrationUseCase {

    User changeRole(Long userId, Role newRole, String actingUsername);

    void deleteUser(Long userId, String actingUsername);
}
