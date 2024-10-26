package io.starseed.asteroidCore.models;

public class Enchantment {
    private final String id;
    private final String name;
    private final int maxLevel;
    private final double priceMultiplier;
    private final String description;

    public Enchantment(String id, String name, int maxLevel, double priceMultiplier, String description) {
        this.id = id;
        this.name = name;
        this.maxLevel = maxLevel;
        this.priceMultiplier = priceMultiplier;
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getMaxLevel() { return maxLevel; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public String getDescription() { return description; }
}
