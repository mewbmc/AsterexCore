package io.starseed.asteroidCore.modules.planet.models;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlanetAutominer {
    private final int id;
    private final UUID planetOwner;
    private final String modelId;
    private final Location location;
    private double incomePerMinute;
    private double totalGenerated;
    private long lastUpdate;

    public PlanetAutominer(int id, UUID planetOwner, String modelId, Location location, double incomePerMinute) {
        this.id = id;
        this.planetOwner = planetOwner;
        this.modelId = modelId;
        this.location = location;
        this.incomePerMinute = incomePerMinute;
        this.totalGenerated = 0;
        this.lastUpdate = System.currentTimeMillis();
    }

    // Getters and setters
    public int getId() { return id; }
    public UUID getPlanetOwner() { return planetOwner; }
    public String getModelId() { return modelId; }
    public Location getLocation() { return location; }
    public double getIncomePerMinute() { return incomePerMinute; }
    public void setIncomePerMinute(double incomePerMinute) { this.incomePerMinute = incomePerMinute; }
    public double getTotalGenerated() { return totalGenerated; }
    public void addToTotalGenerated(double amount) { this.totalGenerated += amount; }
    public long getLastUpdate() { return lastUpdate; }
    public void updateLastUpdate() { this.lastUpdate = System.currentTimeMillis(); }
}

