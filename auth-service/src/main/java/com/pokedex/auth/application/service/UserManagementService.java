package com.pokedex.auth.application.service;

import com.pokedex.auth.application.port.in.UserAdministrationUseCase;
import com.pokedex.auth.application.port.in.UserQueryUseCase;
import com.pokedex.auth.application.port.out.UserRepository;
import com.pokedex.auth.domain.exception.DomainValidationException;
import com.pokedex.auth.domain.exception.OperationNotAllowedException;
import com.pokedex.auth.domain.exception.UserNotFoundException;
import com.pokedex.auth.domain.model.Role;
import com.pokedex.auth.domain.model.User;

import java.util.List;

public class UserManagementService implements UserQueryUseCase, UserAdministrationUseCase {

    private final UserRepository users;

    public UserManagementService(UserRepository users) {
        this.users = users;
    }

    @Override
    public List<User> listUsers() {
        return users.findAll();
    }

    @Override
    public User getById(Long id) {
        return users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getByUsername(String username) {
        return users.findByUsername(User.normalizeUsername(username))
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public User changeRole(Long userId, Role newRole, String actingUsername) {
        if (newRole == null) {
            throw new DomainValidationException("role", "Role is required");
        }
        var target = getById(userId);
        if (target.role() == newRole) {
            return target;
        }
        if (target.username().equals(User.normalizeUsername(actingUsername))) {
            throw new OperationNotAllowedException("You cannot change your own role");
        }
        ensureNotLastAdmin(target);
        return users.save(target.withRole(newRole));
    }

    @Override
    public void deleteUser(Long userId, String actingUsername) {
        var target = getById(userId);
        if (target.username().equals(User.normalizeUsername(actingUsername))) {
            throw new OperationNotAllowedException("You cannot delete your own account");
        }
        ensureNotLastAdmin(target);
        users.deleteById(userId);
    }

    private void ensureNotLastAdmin(User target) {
        if (target.isAdmin() && users.countByRole(Role.ADMIN) <= 1) {
            throw new OperationNotAllowedException("The system must keep at least one administrator");
        }
    }
}
