package com.pokedex.pokemon.infrastructure.web;

import com.pokedex.pokemon.application.port.in.BrowseCatalogUseCase;
import com.pokedex.pokemon.application.port.in.GetCatalogPokemonDetailUseCase;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import com.pokedex.pokemon.infrastructure.web.dto.CatalogDtos.PokemonDetailResponse;
import com.pokedex.pokemon.infrastructure.web.dto.CatalogDtos.PokemonSummaryResponse;
import com.pokedex.pokemon.infrastructure.web.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/** Public, read-only view of the external PokeAPI catalog. */
@RestController
@RequestMapping("/catalog/pokemon")
@Tag(name = "Catalog", description = "Browse PokeAPI (public, cached)")
public class CatalogController {

    private static final CacheControl BROWSER_CACHE = CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic();

    private final BrowseCatalogUseCase browseCatalog;
    private final GetCatalogPokemonDetailUseCase getCatalogPokemonDetail;

    public CatalogController(BrowseCatalogUseCase browseCatalog,
                             GetCatalogPokemonDetailUseCase getCatalogPokemonDetail) {
        this.browseCatalog = browseCatalog;
        this.getCatalogPokemonDetail = getCatalogPokemonDetail;
    }

    @GetMapping
    @Operation(summary = "US01 - Paginated list with sprite, category, weight and abilities")
    public ResponseEntity<PageResponse<PokemonSummaryResponse>> browse(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
        var result = browseCatalog.browse(new PageQuery(page, size));
        return ResponseEntity.ok().cacheControl(BROWSER_CACHE)
                .body(PageResponse.from(result, PokemonSummaryResponse::from));
    }

    @GetMapping("/{idOrName}")
    @Operation(summary = "US02 - Image, stats, description and evolution chain")
    public ResponseEntity<PokemonDetailResponse> detail(@PathVariable String idOrName) {
        var detail = getCatalogPokemonDetail.getDetail(PokemonIdentifier.of(idOrName));
        return ResponseEntity.ok().cacheControl(BROWSER_CACHE).body(PokemonDetailResponse.from(detail));
    }
}
