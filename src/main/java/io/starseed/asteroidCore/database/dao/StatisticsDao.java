package io.starseed.asteroidCore.database.dao;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.PlayerStatistics;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StatisticsDao extends BaseDao {
    private static final String SELECT_STATISTICS = """
        SELECT * FROM player_statistics WHERE uuid = ?
    """;

    private static final String INSERT_STATISTICS = """
        INSERT INTO player_statistics (uuid, blocks_mined, time_played, tokens_earned, money_earned)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        blocks_mined = ?, time_played = ?, tokens_earned = ?, money_earned = ?
    """;

    private static final String UPDATE_STATISTICS = """
        UPDATE player_statistics SET 
        blocks_mined = ?, time_played = ?, tokens_earned = ?, money_earned = ?
        WHERE uuid = ?
    """;

    private static final String DELETE_STATISTICS = """
        DELETE FROM player_statistics WHERE uuid = ?
    """;

    public StatisticsDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
    }

    /**
     * Loads statistics for a player from the database
     * @param uuid The player's UUID
     * @return CompletableFuture containing the statistics, if found
     */
    public CompletableFuture<Optional<PlayerStatistics>> loadStatistics(@NotNull UUID uuid) {
        return executeQuery(
                SELECT_STATISTICS,
                stmt -> stmt.setString(1, uuid.toString()),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToStatistics(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Saves player statistics to the database
     * @param statistics The statistics to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> saveStatistics(@NotNull PlayerStatistics statistics) {
        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_STATISTICS : INSERT_STATISTICS,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, statistics.getPlayerUuid().toString());
                        stmt.setLong(index++, statistics.getBlocksMined());
                        stmt.setLong(index++, statistics.getTimePlayed());
                        stmt.setLong(index++, statistics.getTokensEarned());
                        stmt.setDouble(index++, statistics.getMoneyEarned());
                    }

                    // Update parameters (used for both MySQL and H2)
                    stmt.setLong(index++, statistics.getBlocksMined());
                    stmt.setLong(index++, statistics.getTimePlayed());
                    stmt.setLong(index++, statistics.getTokensEarned());
                    stmt.setDouble(index++, statistics.getMoneyEarned());

                    if (databaseManager.isUsingH2()) {
                        // H2 needs the UUID for the WHERE clause
                        stmt.setString(index, statistics.getPlayerUuid().toString());
                    }
                }
        ).thenApply(result -> null);
    }

    private PlayerStatistics mapResultSetToStatistics(ResultSet rs) throws Exception {
        return new PlayerStatistics(
                UUID.fromString(rs.getString("uuid")),
                rs.getLong("blocks_mined"),
                rs.getLong("time_played"),
                rs.getLong("tokens_earned"),
                rs.getDouble("money_earned")
        );
    }
}