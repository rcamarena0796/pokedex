package com.pokedex.pokemon.infrastructure.web.dto;

import com.pokedex.pokemon.domain.model.EvolutionNode;
import com.pokedex.pokemon.domain.model.PokemonDetail;
import com.pokedex.pokemon.domain.model.PokemonSummary;
import com.pokedex.pokemon.domain.model.Stat;

import java.util.List;

/** Response contracts for the catalog endpoints (User Stories 01 and 02). */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record PokemonSummaryResponse(int id, String name, String spriteUrl, String category, double weightKg,
                                         List<String> types, List<String> abilities) {

        public static PokemonSummaryResponse from(PokemonSummary s) {
            return new PokemonSummaryResponse(s.id(), s.name(), s.spriteUrl(), s.category(), s.weightKg(),
                    s.types(), s.abilities());
        }
    }

    public record StatResponse(String name, int baseValue) {

        public static StatResponse from(Stat stat) {
            return new StatResponse(stat.name(), stat.baseValue());
        }
    }

    public record EvolutionNodeResponse(int speciesId, String name, String imageUrl,
                                        List<EvolutionNodeResponse> evolvesTo) {

        public static EvolutionNodeResponse from(EvolutionNode node) {
            if (node == null) {
                return null;
            }
            return new EvolutionNodeResponse(node.speciesId(), node.name(), node.imageUrl(),
                    node.evolvesTo().stream().map(EvolutionNodeResponse::from).toList());
        }
    }

    public record PokemonDetailResponse(int id, String name, String imageUrl, String spriteUrl, String category,
                                        String description, double heightM, double weightKg,
                                        List<String> types, List<String> abilities, List<StatResponse> stats,
                                        EvolutionNodeResponse evolutionChain) {

        public static PokemonDetailResponse from(PokemonDetail d) {
            return new PokemonDetailResponse(d.id(), d.name(), d.imageUrl(), d.spriteUrl(), d.category(),
                    d.description(), d.heightM(), d.weightKg(), d.types(), d.abilities(),
                    d.stats().stream().map(StatResponse::from).toList(),
                    EvolutionNodeResponse.from(d.evolutionChain()));
        }
    }
}
