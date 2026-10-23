package com.pokedex.pokemon.domain.model;

/** A base stat such as "hp" or "special-attack". */
public record Stat(String name, int baseValue) {
}
