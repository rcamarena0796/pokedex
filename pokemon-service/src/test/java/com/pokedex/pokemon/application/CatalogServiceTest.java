package com.pokedex.pokemon.application;

import com.pokedex.pokemon.TestData;
import com.pokedex.pokemon.application.port.out.PokemonCatalog;
import com.pokedex.pokemon.application.service.CatalogService;
import com.pokedex.pokemon.domain.exception.PokemonNotFoundException;
import com.pokedex.pokemon.domain.model.PageQuery;
import com.pokedex.pokemon.domain.model.PageResult;
import com.pokedex.pokemon.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceTest {

    private final PokemonCatalog catalog = mock(PokemonCatalog.class);
    private final CatalogService service = new CatalogService(catalog);

    @Test
    void shouldBrowseCatalogPage() {
        var query = new PageQuery(0, 2);
        var page = new PageResult<>(List.of(TestData.summary(1, "bulbasaur")), 0, 2, 1302);
        when(catalog.findPage(query)).thenReturn(page);

        assertThat(service.browse(query)).isEqualTo(page);
    }

    @Test
    void shouldReturnDetail() {
        var detail = TestData.detail(6, "charizard");
        when(catalog.findDetail(PokemonIdentifier.of("charizard"))).thenReturn(detail);

        assertThat(service.getDetail(PokemonIdentifier.of("Charizard"))).isEqualTo(detail);
    }

    @Test
    void shouldPropagateNotFound() {
        when(catalog.findDetail(PokemonIdentifier.of("missingno")))
                .thenThrow(new PokemonNotFoundException("missingno"));

        assertThatThrownBy(() -> service.getDetail(PokemonIdentifier.of("missingno")))
                .isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void shouldRequireArguments() {
        assertThatThrownBy(() -> service.browse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> service.getDetail(null)).isInstanceOf(NullPointerException.class);
    }
}
