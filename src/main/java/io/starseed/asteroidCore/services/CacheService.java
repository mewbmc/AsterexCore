package io.starseed.asteroidCore.services;

import com.github.benmanes.caffeine.cache.Cache;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.cache.CacheManager;
import io.starseed.asteroidCore.models.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

public class CacheService {
    private final AsteroidCore plugin;
    private final CacheManager cacheManager;
    private final boolean debug;

    public CacheService(AsteroidCore plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
        this.debug = plugin.getConfig().getBoolean("cache.settings.debug", false);
    }

    // Player Data Caching
    public void cachePlayer(@NotNull PlayerData playerData) {
        try {
            cacheManager.getPlayerCache().put(playerData.getUuid(), playerData);
            logDebug("Cached player data for " + playerData.getUsername());
        } catch (Exception e) {
            logError("Failed to cache player data for " + playerData.getUsername(), e);
        }
    }

    @Nullable
    public PlayerData getCachedPlayer(@NotNull UUID uuid) {
        return cacheManager.getPlayerCache().getIfPresent(uuid);
    }

    public CompletableFuture<PlayerData> getPlayerData(@NotNull UUID uuid) {
        PlayerData cached = getCachedPlayer(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() ->
                plugin.getDatabaseManager().getPlayerDao()
                        .loadPlayer(uuid)
                        .join()
                        .orElseThrow(() -> new RuntimeException("Player data not found for " + uuid))
        ).thenApply(playerData -> {
            cachePlayer(playerData);
            return playerData;
        });
    }

    // Statistics Caching
    public void cacheStatistics(@NotNull PlayerStatistics statistics) {
        try {
            cacheManager.getStatisticsCache().put(statistics.getPlayerUuid(), statistics);
            logDebug("Cached statistics for player " + statistics.getPlayerUuid());
        } catch (Exception e) {
            logError("Failed to cache statistics for player " + statistics.getPlayerUuid(), e);
        }
    }

    @Nullable
    public PlayerStatistics getCachedStatistics(@NotNull UUID uuid) {
        return cacheManager.getStatisticsCache().getIfPresent(uuid);
    }

    public CompletableFuture<PlayerStatistics> getPlayerStatistics(@NotNull UUID uuid) {
        PlayerStatistics cached = getCachedStatistics(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() ->
                plugin.getDatabaseManager().getStatisticsDao()
                        .loadStatistics(uuid)
                        .join()
                        .orElseGet(() -> new PlayerStatistics(uuid))
        ).thenApply(statistics -> {
            cacheStatistics(statistics);
            return statistics;
        });
    }

    // Planet Caching
    public void cachePlanet(@NotNull Planet planet) {
        try {
            cacheManager.getPlanetCache().put(planet.getId(), planet);
            logDebug("Cached planet " + planet.getName());
        } catch (Exception e) {
            logError("Failed to cache planet " + planet.getName(), e);
        }
    }

    @Nullable
    public Planet getCachedPlanet(int id) {
        return cacheManager.getPlanetCache().getIfPresent(id);
    }

    public CompletableFuture<Planet> getPlanet(int id) {
        Planet cached = getCachedPlanet(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() ->
                plugin.getDatabaseManager().getPlanetDao()
                        .loadPlanet(id)
                        .join()
                        .orElseThrow(() -> new RuntimeException("Planet not found with id " + id))
        ).thenApply(planet -> {
            cachePlanet(planet);
            return planet;
        });
    }

    // Private Mine Caching
    public void cachePrivateMine(@NotNull PrivateMine mine) {
        try {
            cacheManager.getPrivateMineCache().put(mine.getId(), mine);
            logDebug("Cached private mine for " + mine.getOwnerUuid());
        } catch (Exception e) {
            logError("Failed to cache private mine for " + mine.getOwnerUuid(), e);
        }
    }

    @Nullable
    public PrivateMine getCachedPrivateMine(int id) {
        return cacheManager.getPrivateMineCache().getIfPresent(id);
    }

    public CompletableFuture<PrivateMine> getPrivateMine(int id) {
        PrivateMine cached = getCachedPrivateMine(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() ->
                plugin.getDatabaseManager().getPrivateMineDao()
                        .loadPrivateMine(id)
                        .join()
                        .orElseThrow(() -> new RuntimeException("Private mine not found with id " + id))
        ).thenApply(mine -> {
            cachePrivateMine(mine);
            return mine;
        });
    }

    // Pickaxe Caching
    public void cachePickaxe(@NotNull Pickaxe pickaxe) {
        try {
            cacheManager.getPickaxeCache().put(pickaxe.getId(), pickaxe);
            logDebug("Cached pickaxe for " + pickaxe.getOwnerUuid());
        } catch (Exception e) {
            logError("Failed to cache pickaxe for " + pickaxe.getOwnerUuid(), e);
        }
    }

    @Nullable
    public Pickaxe getCachedPickaxe(int id) {
        return cacheManager.getPickaxeCache().getIfPresent(id);
    }

    public CompletableFuture<Pickaxe> getPickaxe(int id) {
        Pickaxe cached = getCachedPickaxe(id);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() ->
                plugin.getDatabaseManager().getPickaxeDao()
                        .loadPickaxe(id)
                        .join()
                        .orElseThrow(() -> new RuntimeException("Pickaxe not found with id " + id))
        ).thenApply(pickaxe -> {
            cachePickaxe(pickaxe);
            return pickaxe;
        });
    }

    // Enchantment Caching
    public void cacheEnchantment(@NotNull Enchantment enchantment) {
        try {
            cacheManager.getEnchantmentCache().put(enchantment.getId(), enchantment);
            logDebug("Cached enchantment " + enchantment.getName());
        } catch (Exception e) {
            logError("Failed to cache enchantment " + enchantment.getName(), e);
        }
    }

    @Nullable
    public Enchantment getCachedEnchantment(@NotNull String id) {
        return cacheManager.getEnchantmentCache().getIfPresent(id);
    }

    // Bulk Operations
    public void invalidatePlayerData(@NotNull UUID uuid) {
        cacheManager.getPlayerCache().invalidate(uuid);
        cacheManager.getStatisticsCache().invalidate(uuid);
        logDebug("Invalidated all data for player " + uuid);
    }

    public void preloadPlayerData(@NotNull Collection<UUID> uuids) {
        CompletableFuture.allOf(
                uuids.stream()
                        .map(uuid -> CompletableFuture.runAsync(() -> {
                            try {
                                getPlayerData(uuid);
                                getPlayerStatistics(uuid);
                                logDebug("Preloaded data for player " + uuid);
                            } catch (Exception e) {
                                logError("Failed to preload data for player " + uuid, e);
                            }
                        }))
                        .toArray(CompletableFuture[]::new)
        );
    }

    public void invalidateAll() {
        try {
            cacheManager.invalidateAll();
            logDebug("Invalidated all caches");
        } catch (Exception e) {
            logError("Failed to invalidate all caches", e);
        }
    }

    // Cache Statistics
    public Map<String, Long> getCacheStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("players", cacheManager.getPlayerCache().estimatedSize());
        stats.put("statistics", cacheManager.getStatisticsCache().estimatedSize());
        stats.put("planets", cacheManager.getPlanetCache().estimatedSize());
        stats.put("privateMines", cacheManager.getPrivateMineCache().estimatedSize());
        stats.put("pickaxes", cacheManager.getPickaxeCache().estimatedSize());
        stats.put("enchantments", cacheManager.getEnchantmentCache().estimatedSize());
        return stats;
    }

    // Utility Methods
    private void logDebug(String message) {
        if (debug) {
            plugin.getLogger().info("§b[Cache] " + message);
        }
    }

    private void logError(String message, Exception e) {
        plugin.getLogger().log(Level.SEVERE, "§c[Cache] " + message, e);
    }

    /**
     * Updates an entity in cache and executes an action
     * @param cache The cache to update
     * @param key The cache key
     * @param action The action to perform on the cached entity
     * @param <K> Key type
     * @param <V> Value type
     */
    private <K, V> void updateInCache(Cache<K, V> cache, K key, Consumer<V> action) {
        V value = cache.getIfPresent(key);
        if (value != null) {
            action.accept(value);
            cache.put(key, value);
        }
    }
}