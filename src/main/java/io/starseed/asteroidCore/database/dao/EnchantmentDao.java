package io.starseed.asteroidCore.database.dao;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnchantmentDao extends BaseDao {
    private static final String SELECT_ENCHANTMENT = """
        SELECT * FROM enchantments WHERE id = ?
    """;

    private static final String SELECT_ALL_ENCHANTMENTS = """
        SELECT * FROM enchantments
    """;

    private static final String INSERT_ENCHANTMENT = """
        INSERT INTO enchantments (id, name, max_level, price_multiplier, description)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        name = ?, max_level = ?, price_multiplier = ?, description = ?
    """;

    private static final String UPDATE_ENCHANTMENT = """
        UPDATE enchantments SET 
        name = ?, max_level = ?, price_multiplier = ?, description = ?
        WHERE id = ?
    """;

    private static final String DELETE_ENCHANTMENT = """
        DELETE FROM enchantments WHERE id = ?
    """;

    public EnchantmentDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
    }

    /**
     * Loads an enchantment from the database
     * @param id The enchantment ID
     * @return CompletableFuture containing the enchantment if found
     */
    public CompletableFuture<Optional<Enchantment>> loadEnchantment(@NotNull String id) {
        return executeQuery(
                SELECT_ENCHANTMENT,
                stmt -> stmt.setString(1, id),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToEnchantment(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Loads all enchantments from the database
     * @return CompletableFuture containing list of all enchantments
     */
    public CompletableFuture<List<Enchantment>> loadAllEnchantments() {
        return executeQuery(
                SELECT_ALL_ENCHANTMENTS,
                stmt -> {},
                rs -> {
                    List<Enchantment> enchantments = new ArrayList<>();
                    while (rs.next()) {
                        enchantments.add(mapResultSetToEnchantment(rs));
                    }
                    return enchantments;
                }
        );
    }

    /**
     * Saves an enchantment to the database
     * @param enchantment The enchantment to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> saveEnchantment(@NotNull Enchantment enchantment) {
        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_ENCHANTMENT : INSERT_ENCHANTMENT,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, enchantment.getId());
                        stmt.setString(index++, enchantment.getName());
                        stmt.setInt(index++, enchantment.getMaxLevel());
                        stmt.setDouble(index++, enchantment.getPriceMultiplier());
                        stmt.setString(index++, enchantment.getDescription());
                    }

                    // Update parameters
                    stmt.setString(index++, enchantment.getName());
                    stmt.setInt(index++, enchantment.getMaxLevel());
                    stmt.setDouble(index++, enchantment.getPriceMultiplier());
                    stmt.setString(index++, enchantment.getDescription());

                    if (databaseManager.isUsingH2()) {
                        stmt.setString(index, enchantment.getId());
                    }
                }
        ).thenApply(result -> null);
    }

    /**
     * Deletes an enchantment from the database
     * @param id The ID of the enchantment to delete
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> deleteEnchantment(@NotNull String id) {
        return executeUpdate(
                DELETE_ENCHANTMENT,
                stmt -> stmt.setString(1, id)
        ).thenApply(result -> null);
    }

    private Enchantment mapResultSetToEnchantment(ResultSet rs) throws Exception {
        return new Enchantment(
                rs.getString("id"),
                rs.getString("name"),
                rs.getInt("max_level"),
                rs.getDouble("price_multiplier"),
                rs.getString("description")
        );
    }
}