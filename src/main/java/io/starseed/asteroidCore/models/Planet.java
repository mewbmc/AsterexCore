package io.starseed.asteroidCore.models;

import io.starseed.asteroidCore.modules.planet.models.PlanetStructure;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Planet {
    private final int id;
    private String name;
    private int level;
    private int size;
    private Map<String, Double> resourceRates;
    private Instant lastRegeneration;

    public Planet(int id, String name) {
        this.id = id;
        this.name = name;
        this.level = 1;
        this.size = 100;
        this.resourceRates = new HashMap<>();
        this.lastRegeneration = Instant.now();
    }

    // Full constructor
    public Planet(int id, String name, int level, int size,
                  Map<String, Double> resourceRates, Instant lastRegeneration) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.size = size;
        this.resourceRates = resourceRates;
        this.lastRegeneration = lastRegeneration;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public Map<String, Double> getResourceRates() { return resourceRates; }
    public void setResourceRates(Map<String, Double> resourceRates) { this.resourceRates = resourceRates; }
    public Instant getLastRegeneration() { return lastRegeneration; }
    public void setLastRegeneration(Instant lastRegeneration) { this.lastRegeneration = lastRegeneration; }
    public void addStructure(PlanetStructure structure) {
    }
}
