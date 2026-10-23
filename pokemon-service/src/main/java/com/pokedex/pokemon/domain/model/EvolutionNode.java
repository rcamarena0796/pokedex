package com.pokedex.pokemon.domain.model;

import java.util.List;

/**
 * One species in an evolution chain. A tree, because chains can branch (e.g. Eevee).
 */
public record EvolutionNode(int speciesId, String name, String imageUrl, List<EvolutionNode> evolvesTo) {

    public EvolutionNode {
        evolvesTo = List.copyOf(evolvesTo);
    }
}
