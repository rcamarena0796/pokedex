package com.pokedex.pokemon.infrastructure.web;

import com.pokedex.pokemon.application.port.in.LocalPokemonCommandUseCase;
import com.pokedex.pokemon.application.port.in.LocalPokemonQueryUseCase;
import com.pokedex.pokemon.application.port.in.SyncPokemonUseCase;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.infrastructure.web.dto.LocalPokemonDtos.BatchSyncResponse;
import com.pokedex.pokemon.infrastructure.web.dto.LocalPokemonDtos.LocalPokemonResponse;
import com.pokedex.pokemon.infrastructure.web.dto.LocalPokemonDtos.UpdatePokemonRequest;
import com.pokedex.pokemon.infrastructure.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** CRUD over the local replica. Create = sync from PokeAPI. All routes need a JWT (see SecurityConfig). */
@RestController
@RequestMapping("/pokemon")
@Tag(name = "Local Pokedex", description = "Synchronised Pokemon with proprietary fields")
@SecurityRequirement(name = "bearerAuth")
public class LocalPokemonController {

    private final LocalPokemonQueryUseCase localPokemonQuery;
    private final LocalPokemonCommandUseCase localPokemonCommand;
    private final SyncPokemonUseCase syncPokemon;

    public LocalPokemonController(LocalPokemonQueryUseCase localPokemonQuery,
                                  LocalPokemonCommandUseCase localPokemonCommand,
                                  SyncPokemonUseCase syncPokemon) {
        this.localPokemonQuery = localPokemonQuery;
        this.localPokemonCommand = localPokemonCommand;
        this.syncPokemon = syncPokemon;
    }

    @GetMapping
    @Operation(summary = "List locally stored Pokemon (optional search by name or localized name)")
    public PageResponse<LocalPokemonResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size,
            @RequestParam(required = false) @Size(max = 50) String search) {
        return PageResponse.from(localPokemonQuery.list(new PageQuery(page, size), search), LocalPokemonResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a locally stored Pokemon")
    public LocalPokemonResponse get(@PathVariable @Positive int id) {
        return LocalPokemonResponse.from(localPokemonQuery.get(id));
    }

    @PostMapping("/sync/{idOrName}")
    @Operation(summary = "US03 - Sync one Pokemon from PokeAPI (201 when created, 200 when refreshed)")
    public ResponseEntity<LocalPokemonResponse> syncOne(@PathVariable String idOrName) {
        var result = syncPokemon.sync(PokemonIdentifier.of(idOrName));
        var body = LocalPokemonResponse.from(result.pokemon());
        return result.created()
                ? ResponseEntity.created(URI.create("/pokemon/" + body.id())).body(body)
                : ResponseEntity.ok(body);
    }

    @PostMapping("/sync")
    @Operation(summary = "US03 - Sync a range of Pokedex numbers (max 50 per call)")
    public BatchSyncResponse syncRange(@RequestParam @Positive int fromId, @RequestParam @Positive int toId) {
        return BatchSyncResponse.from(syncPokemon.syncRange(fromId, toId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "US04 - Replace the proprietary fields of a stored Pokemon")
    public LocalPokemonResponse update(@PathVariable @Positive int id,
                                       @Valid @RequestBody UpdatePokemonRequest request) {
        return LocalPokemonResponse.from(localPokemonCommand.updateCustomFields(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a Pokemon from the local store (ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable @Positive int id) {
        localPokemonCommand.delete(id);
        return ResponseEntity.noContent().build();
    }
}
