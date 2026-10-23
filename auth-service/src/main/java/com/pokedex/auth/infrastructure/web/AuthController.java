package com.pokedex.auth.infrastructure.web;

import com.pokedex.auth.application.port.in.AuthenticateUserUseCase;
import com.pokedex.auth.application.port.in.RegisterUserUseCase;
import com.pokedex.auth.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.pokedex.auth.application.port.in.UserQueryUseCase;
import com.pokedex.auth.infrastructure.web.dto.LoginRequest;
import com.pokedex.auth.infrastructure.web.dto.RegisterRequest;
import com.pokedex.auth.infrastructure.web.dto.TokenResponse;
import com.pokedex.auth.infrastructure.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Registration, login and current user")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final AuthenticateUserUseCase authenticateUser;
    private final UserQueryUseCase userQuery;

    public AuthController(RegisterUserUseCase registerUser, AuthenticateUserUseCase authenticateUser,
                          UserQueryUseCase userQuery) {
        this.registerUser = registerUser;
        this.authenticateUser = authenticateUser;
        this.userQuery = userQuery;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (role USER)")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = registerUser.register(
                new RegisterUserCommand(request.username(), request.email(), request.password()));
        return ResponseEntity.created(URI.create("/users/" + user.id())).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for a JWT access token")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUser.authenticate(request.username(), request.password());
        return new TokenResponse(result.accessToken(), "Bearer", result.expiresAt(), UserResponse.from(result.user()));
    }

    @GetMapping("/me")
    @Operation(summary = "Profile of the authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(userQuery.getByUsername(jwt.getSubject()));
    }
}
