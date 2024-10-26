package io.starseed.asteroidCore.services;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.services.CacheService;
import io.starseed.asteroidCore.database.dao.PlayerDao;
import io.starseed.asteroidCore.database.dao.StatisticsDao;
import io.starseed.asteroidCore.models.PlayerData;
import io.starseed.asteroidCore.models.PlayerStatistics;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerService {
    private final AsteroidCore plugin;
    private final PlayerDao playerDao;
    private final StatisticsDao statisticsDao;
    private final CacheService cacheService;

    public PlayerService(AsteroidCore plugin, PlayerDao playerDao, StatisticsDao statisticsDao, CacheService cacheService) {
        this.plugin = plugin;
        this.playerDao = playerDao;
        this.statisticsDao = statisticsDao;
        this.cacheService = cacheService;
    }

    /**
     * Loads or creates player data and statistics
     * @param player The Bukkit player
     * @return CompletableFuture containing the loaded/created player data
     */
    public CompletableFuture<PlayerData> loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String username = player.getName();

        return CompletableFuture.supplyAsync(() -> {
            // Load player data
            PlayerData playerData = playerDao.loadPlayer(uuid)
                    .join()
                    .orElseGet(() -> new PlayerData(uuid, username));

            // Update username if changed
            if (!username.equals(playerData.getUsername())) {
                playerData.setUsername(username);
                playerDao.savePlayer(playerData).join();
            }

            // Load or create statistics
            PlayerStatistics statistics = statisticsDao.loadStatistics(uuid)
                    .join()
                    .orElseGet(() -> new PlayerStatistics(uuid));

            // Cache both
            cacheService.cachePlayer(playerData);
            cacheService.cacheStatistics(statistics);

            return playerData;
        });
    }

    /**
     * Updates a player's balance
     * @param uuid Player UUID
     * @param amount Amount to add (negative for subtract)
     * @return CompletableFuture containing the new balance
     */
    public CompletableFuture<Double> updateBalance(UUID uuid, double amount) {
        return cacheService.getPlayerData(uuid)
                .thenApply(playerData -> {
                    double newBalance = playerData.getBalance() + amount;
                    playerData.setBalance(newBalance);
                    playerDao.savePlayer(playerData);
                    return newBalance;
                });
    }

    /**
     * Updates a player's tokens
     * @param uuid Player UUID
     * @param amount Amount to add (negative for subtract)
     * @return CompletableFuture containing the new token amount
     */
    public CompletableFuture<Long> updateTokens(UUID uuid, long amount) {
        return cacheService.getPlayerData(uuid)
                .thenApply(playerData -> {
                    long newTokens = playerData.getTokens() + amount;
                    playerData.setTokens(newTokens);
                    playerDao.savePlayer(playerData);
                    return newTokens;
                });
    }

    /**
     * Records mining activity for a player
     * @param uuid Player UUID
     * @param blocksMined Number of blocks mined
     * @param moneyEarned Money earned from mining
     * @param tokensEarned Tokens earned from mining
     */
    public void recordMiningActivity(UUID uuid, long blocksMined, double moneyEarned, long tokensEarned) {
        cacheService.getPlayerStatistics(uuid)
                .thenAccept(statistics -> {
                    statistics.setBlocksMined(statistics.getBlocksMined() + blocksMined);
                    statistics.setMoneyEarned(statistics.getMoneyEarned() + moneyEarned);
                    statistics.setTokensEarned(statistics.getTokensEarned() + tokensEarned);
                    statisticsDao.saveStatistics(statistics);
                });
    }

    /**
     * Saves all player data and statistics
     * @param uuid Player UUID
     */
    public CompletableFuture<Void> saveAll(UUID uuid) {
        CompletableFuture<Void> playerSave = cacheService.getPlayerData(uuid)
                .thenCompose(playerDao::savePlayer);

        CompletableFuture<Void> statsSave = cacheService.getPlayerStatistics(uuid)
                .thenCompose(statisticsDao::saveStatistics);

        return CompletableFuture.allOf(playerSave, statsSave);
    }
}