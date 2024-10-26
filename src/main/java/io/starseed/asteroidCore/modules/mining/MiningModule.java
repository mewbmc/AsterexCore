package io.starseed.asteroidCore.modules.mining;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.BaseModule;
import io.starseed.asteroidCore.modules.mining.handlers.BlockBreakHandler;
import io.starseed.asteroidCore.modules.mining.handlers.RewardHandler;
import io.starseed.asteroidCore.modules.mining.listeners.MiningListener;
import io.starseed.asteroidCore.modules.mining.models.MiningStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MiningModule extends BaseModule {
    private final Map<UUID, MiningStats> playerStats;
    private BlockBreakHandler blockBreakHandler;
    private RewardHandler rewardHandler;
    private FileConfiguration config;

    public MiningModule(AsteroidCore plugin) {
        super(plugin);
        this.playerStats = new ConcurrentHashMap<>();
    }

    @Override
    public void enable() {
        // Load configuration
        this.config = plugin.getConfigManager().getModuleConfig("mining").getConfig();

        // Initialize handlers
        this.blockBreakHandler = new BlockBreakHandler(plugin, this);
        this.rewardHandler = new RewardHandler(plugin, this);

        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(
            new MiningListener(plugin, this), plugin);

        // Load all online players' stats
        plugin.getServer().getOnlinePlayers().forEach(this::loadPlayerStats);

        this.enabled = true;
        plugin.getLogger().info("§a[Mining] Module enabled successfully!");
    }

    @Override
    public void disable() {
        // Save all player stats
        saveAllPlayerStats();
        
        this.enabled = false;
        plugin.getLogger().info("§c[Mining] Module disabled!");
    }

    @Override
    public void reloadConfig() {
        plugin.getConfigManager().getModuleConfig("mining").reload();
        this.config = plugin.getConfigManager().getModuleConfig("mining").getConfig();
    }

    public void loadPlayerStats(Player player) {
        plugin.getDatabaseManager().getStatisticsDao().loadStatistics(player.getUniqueId())
            .thenAccept(stats -> {
                MiningStats miningStats = new MiningStats(player.getUniqueId());
                // Convert PlayerStatistics to MiningStats
                if (stats != null) {
                    miningStats = MiningStats.fromPlayerStatistics(stats);
                }
                playerStats.put(player.getUniqueId(), miningStats);
            });
    }

    public void savePlayerStats(UUID playerUuid) {
        MiningStats stats = playerStats.get(playerUuid);
        if (stats != null) {
            plugin.getDatabaseManager().getStatisticsDao()
                .saveStatistics(stats.toPlayerStatistics());
        }
    }

    private void saveAllPlayerStats() {
        playerStats.keySet().forEach(this::savePlayerStats);
    }

    public MiningStats getPlayerStats(UUID playerUuid) {
        return playerStats.computeIfAbsent(playerUuid, uuid -> new MiningStats(uuid));
    }

    public BlockBreakHandler getBlockBreakHandler() {
        return blockBreakHandler;
    }

    public RewardHandler getRewardHandler() {
        return rewardHandler;
    }
}
