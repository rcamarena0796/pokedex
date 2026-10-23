package com.pokedex.auth.infrastructure.web;

import com.pokedex.auth.application.port.in.UserAdministrationUseCase;
import com.pokedex.auth.application.port.in.UserQueryUseCase;
import com.pokedex.auth.infrastructure.web.dto.ChangeRoleRequest;
import com.pokedex.auth.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** User management. Every route here requires the ADMIN role (see SecurityConfig). */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserQueryUseCase userQuery;
    private final UserAdministrationUseCase userAdministration;

    public UserController(UserQueryUseCase userQuery, UserAdministrationUseCase userAdministration) {
        this.userQuery = userQuery;
        this.userAdministration = userAdministration;
    }

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserResponse> list() {
        return userQuery.listUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    public UserResponse get(@PathVariable @Positive Long id) {
        return UserResponse.from(userQuery.getById(id));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change a user's role")
    public UserResponse changeRole(@PathVariable @Positive Long id,
                                   @Valid @RequestBody ChangeRoleRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(userAdministration.changeRole(id, request.role(), jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id, @AuthenticationPrincipal Jwt jwt) {
        userAdministration.deleteUser(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
