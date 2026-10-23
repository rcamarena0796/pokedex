package com.pokedex.auth.infrastructure.web.dto;

import com.pokedex.auth.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {
}
