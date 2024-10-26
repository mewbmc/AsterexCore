package io.starseed.asteroidCore.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String username;
    private double balance;
    private long tokens;
    private int prestigeLevel;
    private String rank;
    private final Instant firstJoin;
    private Instant lastJoin;

    public PlayerData(@NotNull UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.balance = 0.0;
        this.tokens = 0;
        this.prestigeLevel = 0;
        this.rank = "DEFAULT";
        this.firstJoin = Instant.now();
        this.lastJoin = Instant.now();
    }

    // Full constructor
    public PlayerData(UUID uuid, String username, double balance, long tokens,
                      int prestigeLevel, String rank, Instant firstJoin, Instant lastJoin) {
        this.uuid = uuid;
        this.username = username;
        this.balance = balance;
        this.tokens = tokens;
        this.prestigeLevel = prestigeLevel;
        this.rank = rank;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
    }

    // Getters and setters
    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public long getTokens() { return tokens; }
    public void setTokens(long tokens) { this.tokens = tokens; }
    public int getPrestigeLevel() { return prestigeLevel; }
    public void setPrestigeLevel(int prestigeLevel) { this.prestigeLevel = prestigeLevel; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public Instant getFirstJoin() { return firstJoin; }
    public Instant getLastJoin() { return lastJoin; }
    public void setLastJoin(Instant lastJoin) { this.lastJoin = lastJoin; }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}

