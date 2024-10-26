package io.starseed.asteroidCore.models;

import java.util.UUID;

public class PlayerStatistics {
    private final UUID playerUuid;
    private long blocksMined;
    private long timePlayed;
    private long tokensEarned;
    private double moneyEarned;

    public PlayerStatistics(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.blocksMined = 0;
        this.timePlayed = 0;
        this.tokensEarned = 0;
        this.moneyEarned = 0.0;
    }

    // Full constructor
    public PlayerStatistics(UUID playerUuid, long blocksMined, long timePlayed,
                            long tokensEarned, double moneyEarned) {
        this.playerUuid = playerUuid;
        this.blocksMined = blocksMined;
        this.timePlayed = timePlayed;
        this.tokensEarned = tokensEarned;
        this.moneyEarned = moneyEarned;
    }

    // Getters and setters
    public UUID getPlayerUuid() { return playerUuid; }
    public long getBlocksMined() { return blocksMined; }
    public void setBlocksMined(long blocksMined) { this.blocksMined = blocksMined; }
    public long getTimePlayed() { return timePlayed; }
    public void setTimePlayed(long timePlayed) { this.timePlayed = timePlayed; }
    public long getTokensEarned() { return tokensEarned; }
    public void setTokensEarned(long tokensEarned) { this.tokensEarned = tokensEarned; }
    public double getMoneyEarned() { return moneyEarned; }
    public void setMoneyEarned(double moneyEarned) { this.moneyEarned = moneyEarned; }
}
