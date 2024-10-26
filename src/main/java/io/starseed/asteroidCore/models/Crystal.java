package io.starseed.asteroidCore.models;

import java.time.Instant;
import java.util.Map;

public class Crystal {
    private final String id;
    private final String name;
    private final Map<String, Double> bonuses;
    private final long duration;
    private final Instant expiresAt;

    public Crystal(String id, String name, Map<String, Double> bonuses, long duration) {
        this.id = id;
        this.name = name;
        this.bonuses = bonuses;
        this.duration = duration;
        this.expiresAt = Instant.now().plusSeconds(duration);
    }

    // Full constructor
    public Crystal(String id, String name, Map<String, Double> bonuses, long duration, Instant expiresAt) {
        this.id = id;
        this.name = name;
        this.bonuses = bonuses;
        this.duration = duration;
        this.expiresAt = expiresAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, Double> getBonuses() { return bonuses; }
    public long getDuration() { return duration; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
