package com.pokedex.pokemon.infrastructure.web.dto;

import com.pokedex.pokemon.application.port.in.LocalPokemonCommandUseCase.UpdateCustomFieldsCommand;
import com.pokedex.pokemon.application.port.in.SyncPokemonUseCase.BatchSyncResult;
import com.pokedex.pokemon.domain.model.LocalPokemon;
import com.pokedex.pokemon.infrastructure.web.dto.CatalogDtos.StatResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Request/response contracts for the local store (User Stories 03 and 04). */
public final class LocalPokemonDtos {

    private LocalPokemonDtos() {
    }

    public record LocalPokemonResponse(int id, String name, String category, String description,
                                       double heightM, double weightKg, List<String> types, List<String> abilities,
                                       List<StatResponse> stats, String spriteUrl, String imageUrl,
                                       String localizedName, String region, List<String> tags, String notes,
                                       Instant syncedAt, Instant updatedAt) {

        public static LocalPokemonResponse from(LocalPokemon p) {
            var custom = p.customFields();
            return new LocalPokemonResponse(p.id(), p.name(), p.category(), p.description(), p.heightM(),
                    p.weightKg(), p.types(), p.abilities(), p.stats().stream().map(StatResponse::from).toList(),
                    p.spriteUrl(), p.imageUrl(), custom.localizedName(), custom.region(), custom.tags(),
                    custom.notes(), p.syncedAt(), p.updatedAt());
        }
    }

    /**
     * Full replacement of the proprietary fields (PUT semantics: omitted fields are cleared).
     * Shape checks live here (400 before any work); business rules are re-checked in the domain.
     */
    public record UpdatePokemonRequest(
            @Size(max = 60) String localizedName,
            @Size(max = 40) String region,
            @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags,
            @Size(max = 500) String notes) {

        public UpdateCustomFieldsCommand toCommand() {
            return new UpdateCustomFieldsCommand(localizedName, region, tags, notes);
        }
    }

    public record BatchSyncResponse(List<Integer> created, List<Integer> updated, Map<Integer, String> failed) {

        public static BatchSyncResponse from(BatchSyncResult result) {
            return new BatchSyncResponse(result.created(), result.updated(), result.failed());
        }
    }
}
