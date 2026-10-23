package com.pokedex.pokemon.domain.model;

import com.pokedex.pokemon.domain.exception.DomainValidationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Proprietary fields that exist only in our local copy (User Story 03/04):
 * localised name, geographical metadata and internal classification tags.
 * Values are normalised (trimmed, blank to null, tags lower-cased and de-duplicated).
 */
public record CustomFields(String localizedName, String region, List<String> tags, String notes) {

    public static final int MAX_TAGS = 10;
    private static final Pattern TAG = Pattern.compile("^[a-z0-9][a-z0-9-]{0,29}$");
    private static final Pattern REGION = Pattern.compile("^[\\p{L} .'-]{1,40}$");

    public CustomFields {
        localizedName = blankToNull(localizedName);
        region = blankToNull(region);
        notes = blankToNull(notes);
        tags = normalizeTags(tags);

        if (localizedName != null && localizedName.length() > 60) {
            throw new DomainValidationException("localizedName", "Localized name must be at most 60 characters");
        }
        if (region != null && !REGION.matcher(region).matches()) {
            throw new DomainValidationException("region",
                    "Region must be at most 40 characters: letters, spaces, dots, apostrophes or hyphens");
        }
        if (notes != null && notes.length() > 500) {
            throw new DomainValidationException("notes", "Notes must be at most 500 characters");
        }
    }

    public static CustomFields empty() {
        return new CustomFields(null, null, List.of(), null);
    }

    private static List<String> normalizeTags(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        var unique = new LinkedHashSet<String>();
        for (String tag : raw) {
            var normalized = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
            if (!TAG.matcher(normalized).matches()) {
                throw new DomainValidationException("tags",
                        "Tag '%s' is invalid: use 1-30 lowercase letters, digits or hyphens".formatted(tag));
            }
            unique.add(normalized);
        }
        if (unique.size() > MAX_TAGS) {
            throw new DomainValidationException("tags", "At most %d tags are allowed".formatted(MAX_TAGS));
        }
        return List.copyOf(unique);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
