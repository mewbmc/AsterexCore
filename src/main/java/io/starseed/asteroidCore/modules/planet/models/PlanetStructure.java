package io.starseed.asteroidCore.modules.planet.models;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlanetStructure {
    private final int id;
    private final UUID planetOwner;
    private final String structureType;
    private final String modelId;
    private final Location location;
    private final Map<String, Double> effects;
    private boolean active;

    public PlanetStructure(int id, UUID planetOwner, String structureType, String modelId, Location location) {
        this.id = id;
        this.planetOwner = planetOwner;
        this.structureType = structureType;
        this.modelId = modelId;
        this.location = location;
        this.effects = new HashMap<>();
        this.active = true;
    }

    public void addEffect(String effect, double value) {
        effects.put(effect, value);
    }

    // Getters
    public int getId() { return id; }
    public UUID getPlanetOwner() { return planetOwner; }
    public String getStructureType() { return structureType; }
    public String getModelId() { return modelId; }
    public Location getLocation() { return location; }
    public Map<String, Double> getEffects() { return new HashMap<>(effects); }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
