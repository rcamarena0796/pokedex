package com.pokedex.pokemon.domain.model;

import com.pokedex.pokemon.domain.exception.DomainValidationException;

/** Zero-based page request. Invariants are enforced on construction. */
public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 50;

    public PageQuery {
        if (page < 0) {
            throw new DomainValidationException("page", "Page must be zero or greater");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new DomainValidationException("size", "Size must be between 1 and " + MAX_SIZE);
        }
    }

    public int offset() {
        return page * size;
    }
}
