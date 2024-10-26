package io.starseed.asteroidCore.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.Crystal;
import io.starseed.asteroidCore.models.Enchantment;
import io.starseed.asteroidCore.models.Pickaxe;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PickaxeDao extends BaseDao {
    private static final String SELECT_PICKAXE = """
        SELECT * FROM pickaxes WHERE id = ?
    """;

    private static final String SELECT_PICKAXES_BY_OWNER = """
        SELECT * FROM pickaxes WHERE owner_uuid = ?
    """;

    private static final String INSERT_PICKAXE = """
        INSERT INTO pickaxes (owner_uuid, name, level, experience, enchantments, skin, crystals)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        name = ?, level = ?, experience = ?, enchantments = ?, skin = ?, crystals = ?
    """;

    private static final String UPDATE_PICKAXE = """
        UPDATE pickaxes SET 
        name = ?, level = ?, experience = ?, enchantments = ?, skin = ?, crystals = ?
        WHERE id = ?
    """;

    private static final String DELETE_PICKAXE = """
        DELETE FROM pickaxes WHERE id = ?
    """;

    private final Gson gson;
    private final Type enchantmentsType;
    private final Type crystalsType;

    public PickaxeDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
        this.gson = new Gson();
        this.enchantmentsType = new TypeToken<Map<String, Integer>>(){}.getType();
        this.crystalsType = new TypeToken<List<Crystal>>(){}.getType();
    }

    /**
     * Loads a pickaxe from the database
     * @param id The pickaxe ID
     * @return CompletableFuture containing the pickaxe if found
     */
    public CompletableFuture<Optional<Pickaxe>> loadPickaxe(int id) {
        return executeQuery(
                SELECT_PICKAXE,
                stmt -> stmt.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPickaxe(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Loads all pickaxes owned by a player
     * @param ownerUuid The UUID of the owner
     * @return CompletableFuture containing list of pickaxes
     */
    public CompletableFuture<List<Pickaxe>> loadPickaxesByOwner(@NotNull UUID ownerUuid) {
        return executeQuery(
                SELECT_PICKAXES_BY_OWNER,
                stmt -> stmt.setString(1, ownerUuid.toString()),
                rs -> {
                    List<Pickaxe> pickaxes = new ArrayList<>();
                    while (rs.next()) {
                        pickaxes.add(mapResultSetToPickaxe(rs));
                    }
                    return pickaxes;
                }
        );
    }

    /**
     * Saves a pickaxe to the database
     * @param pickaxe The pickaxe to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> savePickaxe(@NotNull Pickaxe pickaxe) {
        Map<String, Integer> enchantmentLevels = new HashMap<>();
        pickaxe.getEnchantments().forEach((enchant, level) ->
                enchantmentLevels.put(enchant.getId(), level));

        String enchantmentsJson = gson.toJson(enchantmentLevels, enchantmentsType);
        String crystalsJson = gson.toJson(pickaxe.getCrystals(), crystalsType);

        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_PICKAXE : INSERT_PICKAXE,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, pickaxe.getOwnerUuid().toString());
                        stmt.setString(index++, pickaxe.getName());
                        stmt.setInt(index++, pickaxe.getLevel());
                        stmt.setLong(index++, pickaxe.getExperience());
                        stmt.setString(index++, enchantmentsJson);
                        stmt.setString(index++, pickaxe.getSkin());
                        stmt.setString(index++, crystalsJson);
                    }

                    // Update parameters
                    stmt.setString(index++, pickaxe.getName());
                    stmt.setInt(index++, pickaxe.getLevel());
                    stmt.setLong(index++, pickaxe.getExperience());
                    stmt.setString(index++, enchantmentsJson);
                    stmt.setString(index++, pickaxe.getSkin());
                    stmt.setString(index++, crystalsJson);

                    if (databaseManager.isUsingH2()) {
                        stmt.setInt(index, pickaxe.getId());
                    }
                }
        ).thenApply(result -> null);
    }

    /**
     * Deletes a pickaxe from the database
     * @param id The ID of the pickaxe to delete
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> deletePickaxe(int id) {
        return executeUpdate(
                DELETE_PICKAXE,
                stmt -> stmt.setInt(1, id)
        ).thenApply(result -> null);
    }

    private Pickaxe mapResultSetToPickaxe(ResultSet rs) throws Exception {
        String enchantmentsJson = rs.getString("enchantments");
        Map<String, Integer> enchantmentLevels = enchantmentsJson != null ?
                gson.fromJson(enchantmentsJson, enchantmentsType) :
                new HashMap<>();

        Map<Enchantment, Integer> enchantments = new HashMap<>();
        EnchantmentDao enchantmentDao = plugin.getDatabaseManager().getEnchantmentDao();

        for (Map.Entry<String, Integer> entry : enchantmentLevels.entrySet()) {
            enchantmentDao.loadEnchantment(entry.getKey())
                    .join()
                    .ifPresent(enchantment ->
                            enchantments.put(enchantment, entry.getValue()));
        }

        String crystalsJson = rs.getString("crystals");
        List<Crystal> crystals = crystalsJson != null ?
                gson.fromJson(crystalsJson, crystalsType) :
                new ArrayList<>();

        return new Pickaxe(
                rs.getInt("id"),
                UUID.fromString(rs.getString("owner_uuid")),
                rs.getString("name"),
                rs.getInt("level"),
                rs.getLong("experience"),
                enchantments,
                rs.getString("skin"),
                crystals
        );
    }
}