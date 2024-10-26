package io.starseed.asteroidCore.modules.mining.models;

import io.starseed.asteroidCore.models.PlayerStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MiningStats {
    private final UUID playerUuid;
    private long blocksBroken;
    private double totalValue;
    private double lastRewardThreshold;
    private Map<String, Long> blockTypeCounts;
    private Map<String, Double> multipliers;

    public static MiningStats fromPlayerStatistics(Optional<PlayerStatistics> stats) {
        MiningStats miningStats = new MiningStats(stats.getPlayerUuid());
        miningStats.blocksBroken = stats.getBlocksMined();
        miningStats.totalValue = stats.getTotalValue();
        // Convert other relevant statistics
        return miningStats;
    }

    public MiningStats(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.blocksBroken = 0;
        this.totalValue = 0.0;
        this.lastRewardThreshold = 0.0;
        this.blockTypeCounts = new HashMap<>();
        this.multipliers = new HashMap<>();
    }

    public void incrementBlocksBroken() {
        this.blocksBroken++;
    }

    public void incrementBlockType(String blockType) {
        blockTypeCounts.merge(blockType, 1L, Long::sum);
    }

    public void incrementTotalValue(double value) {
        this.totalValue += value;
    }

    public void addMultiplier(String type, double value) {
        multipliers.put(type, value);
    }

    public double calculateTotalMultiplier() {
        return multipliers.values().stream()
                .reduce(1.0, (a, b) -> a * b);
    }

    public PlayerStatistics toPlayerStatistics() {
        PlayerStatistics stats = new PlayerStatistics();
        stats.setPlayerUuid(playerUuid);
        stats.setBlocksMined(blocksBroken);
        stats.setTotalValue(totalValue);
        // Add other conversions as needed
        return stats;
    }

    // Getters and setters
    public UUID getPlayerUuid() { return playerUuid; }
    public long getBlocksBroken() { return blocksBroken; }
    public double getTotalValue() { return totalValue; }
    public double getLastRewardThreshold() { return lastRewardThreshold; }
    public void setLastRewardThreshold(double lastRewardThreshold) { this.lastRewardThreshold = lastRewardThreshold; }
    public Map<String, Long> getBlockTypeCounts() { return new HashMap<>(blockTypeCounts); }
    public Map<String, Double> getMultipliers() { return new HashMap<>(multipliers); }
}
