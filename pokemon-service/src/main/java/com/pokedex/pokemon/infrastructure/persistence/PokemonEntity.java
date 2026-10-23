package com.pokedex.pokemon.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;

/**
 * Primary entity of the local store. The primary key is the national Pokedex number,
 * which makes synchronisation idempotent.
 */
@Entity
@Table(name = "pokemon")
public class PokemonEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 60)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(name = "height_m")
    private Double heightM;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Convert(converter = StringListConverter.class)
    @Column(length = 200)
    private List<String> types;

    @Convert(converter = StringListConverter.class)
    @Column(length = 500)
    private List<String> abilities;

    private Integer hp;
    private Integer attack;
    private Integer defense;

    @Column(name = "special_attack")
    private Integer specialAttack;

    @Column(name = "special_defense")
    private Integer specialDefense;

    private Integer speed;

    @Column(name = "sprite_url", length = 300)
    private String spriteUrl;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    // --- proprietary fields ---

    @Column(name = "localized_name", length = 60)
    private String localizedName;

    @Column(length = 40)
    private String region;

    @Convert(converter = StringListConverter.class)
    @Column(length = 400)
    private List<String> tags;

    @Column(length = 500)
    private String notes;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PokemonEntity() {
    }

    // Getters and setters are only used by the repository adapter's mapper.

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getHeightM() { return heightM; }
    public void setHeightM(Double heightM) { this.heightM = heightM; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }
    public List<String> getAbilities() { return abilities; }
    public void setAbilities(List<String> abilities) { this.abilities = abilities; }
    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }
    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }
    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) { this.defense = defense; }
    public Integer getSpecialAttack() { return specialAttack; }
    public void setSpecialAttack(Integer specialAttack) { this.specialAttack = specialAttack; }
    public Integer getSpecialDefense() { return specialDefense; }
    public void setSpecialDefense(Integer specialDefense) { this.specialDefense = specialDefense; }
    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }
    public String getSpriteUrl() { return spriteUrl; }
    public void setSpriteUrl(String spriteUrl) { this.spriteUrl = spriteUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLocalizedName() { return localizedName; }
    public void setLocalizedName(String localizedName) { this.localizedName = localizedName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
