package io.starseed.asteroidCore.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.PrivateMine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PrivateMineDao extends BaseDao {
    private static final String SELECT_MINE = """
        SELECT * FROM private_mines WHERE id = ?
    """;

    private static final String SELECT_MINES_BY_OWNER = """
        SELECT * FROM private_mines WHERE owner_uuid = ?
    """;

    private static final String INSERT_MINE = """
        INSERT INTO private_mines (owner_uuid, name, size, level, is_public, resource_rates, whitelist, last_regeneration)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        name = ?, size = ?, level = ?, is_public = ?, resource_rates = ?, whitelist = ?, last_regeneration = ?
    """;

    private static final String UPDATE_MINE = """
        UPDATE private_mines SET 
        name = ?, size = ?, level = ?, is_public = ?, resource_rates = ?, whitelist = ?, last_regeneration = ?
        WHERE id = ?
    """;

    private static final String DELETE_MINE = """
        DELETE FROM private_mines WHERE id = ?
    """;

    private final Gson gson;
    private final Type resourceRatesType;
    private final Type whitelistType;

    public PrivateMineDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
        this.gson = new Gson();
        this.resourceRatesType = new TypeToken<Map<String, Double>>(){}.getType();
        this.whitelistType = new TypeToken<Set<UUID>>(){}.getType();
    }

    /**
     * Loads a private mine from the database
     * @param id The mine ID
     * @return CompletableFuture containing the mine if found
     */
    public CompletableFuture<Optional<PrivateMine>> loadPrivateMine(int id) {
        return executeQuery(
                SELECT_MINE,
                stmt -> stmt.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPrivateMine(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Loads all private mines owned by a player
     * @param ownerUuid The UUID of the owner
     * @return CompletableFuture containing list of mines
     */
    public CompletableFuture<List<PrivateMine>> loadMinesByOwner(@NotNull UUID ownerUuid) {
        return executeQuery(
                SELECT_MINES_BY_OWNER,
                stmt -> stmt.setString(1, ownerUuid.toString()),
                rs -> {
                    List<PrivateMine> mines = new ArrayList<>();
                    while (rs.next()) {
                        mines.add(mapResultSetToPrivateMine(rs));
                    }
                    return mines;
                }
        );
    }

    /**
     * Saves a private mine to the database
     * @param mine The mine to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> savePrivateMine(@NotNull PrivateMine mine) {
        String resourceRatesJson = gson.toJson(mine.getResourceRates(), resourceRatesType);
        String whitelistJson = gson.toJson(mine.getWhitelist(), whitelistType);

        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_MINE : INSERT_MINE,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, mine.getOwnerUuid().toString());
                        stmt.setString(index++, mine.getName());
                        stmt.setInt(index++, mine.getSize());
                        stmt.setInt(index++, mine.getLevel());
                        stmt.setBoolean(index++, mine.isPublic());
                        stmt.setString(index++, resourceRatesJson);
                        stmt.setString(index++, whitelistJson);
                        stmt.setTimestamp(index++, Timestamp.from(mine.getLastRegeneration()));
                    }

                    // Update parameters
                    stmt.setString(index++, mine.getName());
                    stmt.setInt(index++, mine.getSize());
                    stmt.setInt(index++, mine.getLevel());
                    stmt.setBoolean(index++, mine.isPublic());
                    stmt.setString(index++, resourceRatesJson);
                    stmt.setString(index++, whitelistJson);
                    stmt.setTimestamp(index++, Timestamp.from(mine.getLastRegeneration()));

                    if (databaseManager.isUsingH2()) {
                        stmt.setInt(index, mine.getId());
                    }
                }
        ).thenApply(result -> null);
    }

    /**
     * Deletes a private mine from the database
     * @param id The ID of the mine to delete
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> deletePrivateMine(int id) {
        return executeUpdate(
                DELETE_MINE,
                stmt -> stmt.setInt(1, id)
        ).thenApply(result -> null);
    }

    @SuppressWarnings("unchecked")
    private PrivateMine mapResultSetToPrivateMine(ResultSet rs) throws Exception {
        String resourceRatesJson = rs.getString("resource_rates");
        Map<String, Double> resourceRates = resourceRatesJson != null ?
                gson.fromJson(resourceRatesJson, resourceRatesType) :
                new HashMap<>();

        String whitelistJson = rs.getString("whitelist");
        Set<UUID> whitelist = whitelistJson != null ?
                gson.fromJson(whitelistJson, whitelistType) :
                new HashSet<>();

        return new PrivateMine(
                rs.getInt("id"),
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getString("name"),
                rs.getInt("size"),
                rs.getInt("level"),
                rs.getBoolean("is_public"),
                resourceRates,
                whitelist,
                rs.getTimestamp("last_regeneration").toInstant()
        );
    }
}