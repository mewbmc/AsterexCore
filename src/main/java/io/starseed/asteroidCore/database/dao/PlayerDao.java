package io.starseed.asteroidCore.database.dao;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDao extends BaseDao {
    private static final String SELECT_PLAYER = """
        SELECT * FROM players WHERE uuid = ?
    """;

    private static final String INSERT_PLAYER = """
        INSERT INTO players (uuid, username, first_join, last_join, balance, tokens, prestige_level, rank)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        username = ?, last_join = ?, balance = ?, tokens = ?, prestige_level = ?, rank = ?
    """;

    private static final String UPDATE_PLAYER = """
        UPDATE players SET 
        username = ?, last_join = ?, balance = ?, tokens = ?, prestige_level = ?, rank = ?
        WHERE uuid = ?
    """;

    private static final String DELETE_PLAYER = """
        DELETE FROM players WHERE uuid = ?
    """;

    public PlayerDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
    }

    /**
     * Loads a player's data from the database
     * @param uuid The player's UUID
     * @return CompletableFuture containing the player data, if found
     */
    public CompletableFuture<Optional<PlayerData>> loadPlayer(@NotNull UUID uuid) {
        return executeQuery(
                SELECT_PLAYER,
                stmt -> stmt.setString(1, uuid.toString()),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlayer(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Saves a player's data to the database
     * @param player The player data to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> savePlayer(@NotNull PlayerData player) {
        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_PLAYER : INSERT_PLAYER,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, player.getUuid().toString());
                        stmt.setString(index++, player.getUsername());
                        stmt.setTimestamp(index++, Timestamp.from(player.getFirstJoin()));
                        stmt.setTimestamp(index++, Timestamp.from(player.getLastJoin()));
                        stmt.setDouble(index++, player.getBalance());
                        stmt.setLong(index++, player.getTokens());
                        stmt.setInt(index++, player.getPrestigeLevel());
                        stmt.setString(index++, player.getRank());
                    }

                    // Update parameters (used for both MySQL and H2)
                    stmt.setString(index++, player.getUsername());
                    stmt.setTimestamp(index++, Timestamp.from(player.getLastJoin()));
                    stmt.setDouble(index++, player.getBalance());
                    stmt.setLong(index++, player.getTokens());
                    stmt.setInt(index++, player.getPrestigeLevel());
                    stmt.setString(index++, player.getRank());

                    if (databaseManager.isUsingH2()) {
                        // H2 needs the UUID for the WHERE clause
                        stmt.setString(index, player.getUuid().toString());
                    }
                }
        ).thenApply(result -> null);
    }

    /**
     * Deletes a player's data from the database
     * @param uuid The UUID of the player to delete
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> deletePlayer(@NotNull UUID uuid) {
        return executeUpdate(
                DELETE_PLAYER,
                stmt -> stmt.setString(1, uuid.toString())
        ).thenApply(result -> null);
    }

    private PlayerData mapResultSetToPlayer(ResultSet rs) throws Exception {
        return new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("username"),
                rs.getDouble("balance"),
                rs.getLong("tokens"),
                rs.getInt("prestige_level"),
                rs.getString("rank"),
                rs.getTimestamp("first_join").toInstant(),
                rs.getTimestamp("last_join").toInstant()
        );
    }
}