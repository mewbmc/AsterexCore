package io.starseed.asteroidCore.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.models.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CacheManager {
    private final AsteroidCore plugin;
    private final FileConfiguration config;
    private final boolean debug;

    // Core data caches
    private final Cache<UUID, PlayerData> playerCache;
    private final Cache<UUID, PlayerStatistics> statisticsCache;
    private final Cache<Integer, Planet> planetCache;
    private final Cache<Integer, PrivateMine> privateMineCache;
    private final Cache<Integer, Pickaxe> pickaxeCache;
    private final Cache<String, Enchantment> enchantmentCache;

    public CacheManager(AsteroidCore plugin) {
        this.plugin = plugin;

        // Load cache configuration
        File configFile = new File(plugin.getDataFolder(), "cache.yml");
        if (!configFile.exists()) {
            plugin.saveResource("cache.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.debug = config.getBoolean("cache.settings.debug", false);

        // Initialize caches
        this.playerCache = createCache("player");
        this.statisticsCache = createCache("statistics");
        this.planetCache = createCache("planet");
        this.privateMineCache = createCache("private-mine");
        this.pickaxeCache = createCache("pickaxe");
        this.enchantmentCache = createCache("enchantment");

        if (debug) {
            plugin.getLogger().info("§b[Cache] Cache system initialized with debug mode enabled");
        }
    }

    private <K, V> Cache<K, V> createCache(String configSection) {
        int maxSize = config.getInt("cache." + configSection + ".maximum-size", 1000);
        int expireAfterAccess = config.getInt("cache." + configSection + ".expire-after-access", 30);
        int expireAfterWrite = config.getInt("cache." + configSection + ".expire-after-write", 60);

        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES);

        if (debug) {
            plugin.getLogger().info(String.format(
                    "§b[Cache] Created cache for %s (size: %d, access: %dm, write: %dm)",
                    configSection, maxSize, expireAfterAccess, expireAfterWrite
            ));
        }

        return builder.build();
    }

    // Getter methods for caches
    public Cache<UUID, PlayerData> getPlayerCache() { return playerCache; }
    public Cache<UUID, PlayerStatistics> getStatisticsCache() { return statisticsCache; }
    public Cache<Integer, Planet> getPlanetCache() { return planetCache; }
    public Cache<Integer, PrivateMine> getPrivateMineCache() { return privateMineCache; }
    public Cache<Integer, Pickaxe> getPickaxeCache() { return pickaxeCache; }
    public Cache<String, Enchantment> getEnchantmentCache() { return enchantmentCache; }

    public void logDebug(String message) {
        if (debug) {
            plugin.getLogger().info("§b[Cache] " + message);
        }
    }

    /**
     * Invalidates all caches
     */
    public void invalidateAll() {
        playerCache.invalidateAll();
        statisticsCache.invalidateAll();
        planetCache.invalidateAll();
        privateMineCache.invalidateAll();
        pickaxeCache.invalidateAll();
        enchantmentCache.invalidateAll();

        logDebug("All caches have been invalidated");
    }

    /**
     * Cleans up resources and prepares for shutdown
     */
    public void shutdown() {
        invalidateAll();
        logDebug("Cache system has been shut down");
    }
}