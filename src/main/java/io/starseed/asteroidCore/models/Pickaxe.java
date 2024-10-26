package io.starseed.asteroidCore.models;

import java.util.*;

public class Pickaxe {
    private final int id;
    private final UUID ownerUuid;
    private String name;
    private int level;
    private long experience;
    private Map<Enchantment, Integer> enchantments;
    private String skin;
    private List<Crystal> crystals;

    public Pickaxe(int id, UUID ownerUuid, String name) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.enchantments = new HashMap<>();
        this.skin = "DEFAULT";
        this.crystals = new ArrayList<>();
    }

    // Full constructor
    public Pickaxe(int id, UUID ownerUuid, String name, int level, long experience,
                   Map<Enchantment, Integer> enchantments, String skin, List<Crystal> crystals) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.level = level;
        this.experience = experience;
        this.enchantments = enchantments;
        this.skin = skin;
        this.crystals = crystals;
    }

    // Getters and setters
    public int getId() { return id; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getExperience() { return experience; }
    public void setExperience(long experience) { this.experience = experience; }
    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }
    public void setEnchantments(Map<Enchantment, Integer> enchantments) { this.enchantments = enchantments; }
    public String getSkin() { return skin; }
    public void setSkin(String skin) { this.skin = skin; }
    public List<Crystal> getCrystals() { return crystals; }
    public void setCrystals(List<Crystal> crystals) { this.crystals = crystals; }
}
