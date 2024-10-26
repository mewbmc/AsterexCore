package io.starseed.asteroidCore.modules.mining.handlers;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.mining.MiningModule;
import io.starseed.asteroidCore.modules.mining.models.MiningStats;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RewardHandler {
    private final AsteroidCore plugin;
    private final MiningModule miningModule;

    public RewardHandler(AsteroidCore plugin, MiningModule miningModule) {
        this.plugin = plugin;
        this.miningModule = miningModule;
    }

    public CompletableFuture<Void> handleReward(Player player, double value) {
        // Get player's mining stats
        MiningStats stats = miningModule.getPlayerStats(player.getUniqueId());

        // Update player's mining stats
        stats.incrementTotalValue(value);

        // Check if player has reached a reward threshold
        List<Double> rewardThresholds = getRewardThresholds();
        for (double threshold : rewardThresholds) {
            if (stats.getTotalValue() >= threshold) {
                // Grant reward
                grantReward(player, threshold);

                // Update stats
                stats.setLastRewardThreshold(threshold);
            }
        }

        // Save player's updated mining stats
        plugin.getDatabaseManager().getStatisticsDao().saveStatistics(stats.toPlayerStatistics());

        return CompletableFuture.completedFuture(null);
    }

    private List<Double> getRewardThresholds() {
        // Load reward thresholds from configuration
        return plugin.getConfigManager().getModuleConfig("mining").getConfig().getDoubleList("reward_thresholds");
    }

    private void grantReward(Player player, double threshold) {
        // Grant reward to the player
        // Example: Give the player a random item from a list of rewards
        List<ItemStack> rewards = getRewardItems(threshold);
        rewards.forEach(player.getInventory()::addItem);
    }

    private List<ItemStack> getRewardItems(double threshold) {
        // Load reward items from configuration based on the threshold
        return plugin.getConfigManager().getModuleConfig("mining").getConfig().getList("rewards." + threshold, ItemStack.class);
    }
}
