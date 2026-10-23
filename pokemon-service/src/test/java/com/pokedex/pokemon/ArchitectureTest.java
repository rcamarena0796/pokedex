package com.pokedex.pokemon;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** Enforces the Clean Architecture dependency rule: dependencies only point inwards. */
class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.pokedex.pokemon");

    @Test
    void layersShouldOnlyDependInwards() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .check(CLASSES);
    }

    @Test
    void domainAndApplicationShouldBeFrameworkFree() {
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..")
                .check(CLASSES);
    }

    @Test
    void webLayerShouldNotTalkToPersistenceOrPokeApiDirectly() {
        noClasses().that().resideInAPackage("..infrastructure.web..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence..", "..infrastructure.pokeapi..")
                .check(CLASSES);
    }
}
