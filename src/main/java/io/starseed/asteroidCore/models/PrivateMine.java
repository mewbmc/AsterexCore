package io.starseed.asteroidCore.models;

import org.bukkit.Material;
import java.time.Instant;
import java.util.*;

public class PrivateMine {
    private final int id;
    private final UUID ownerUuid;
    private String name;
    private int size;
    private int level;
    private boolean isPublic;
    private Map<String, Double> resourceRates;
    private Set<UUID> whitelist;
    private Instant lastRegeneration;

    public PrivateMine(int id, UUID ownerUuid, String name) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.size = 25;
        this.level = 1;
        this.isPublic = false;
        this.resourceRates = new HashMap<>();
        this.whitelist = new HashSet<>();
        this.lastRegeneration = Instant.now();
    }

    // Full constructor
    public PrivateMine(int id, UUID ownerUuid, String name, int size, int level,
                       boolean isPublic, Map<String, Double> resourceRates,
                       Set<UUID> whitelist, Instant lastRegeneration) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.size = size;
        this.level = level;
        this.isPublic = isPublic;
        this.resourceRates = resourceRates;
        this.whitelist = whitelist;
        this.lastRegeneration = lastRegeneration;
    }

    // Getters and setters
    public int getId() { return id; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public Map<String, Double> getResourceRates() { return resourceRates; }
    public void setResourceRates(Map<String, Double> resourceRates) { this.resourceRates = resourceRates; }
    public Set<UUID> getWhitelist() { return whitelist; }
    public void setWhitelist(Set<UUID> whitelist) { this.whitelist = whitelist; }
    public Instant getLastRegeneration() { return lastRegeneration; }
    public void setLastRegeneration(Instant lastRegeneration) { this.lastRegeneration = lastRegeneration; }

    public boolean canAccess(UUID playerUuid) {
        return ownerUuid.equals(playerUuid) || isPublic || whitelist.contains(playerUuid);
    }
}

