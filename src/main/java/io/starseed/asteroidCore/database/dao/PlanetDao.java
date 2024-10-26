package io.starseed.asteroidCore.database.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.models.Planet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlanetDao extends BaseDao {
    private static final String SELECT_PLANET = """
        SELECT * FROM planets WHERE id = ?
    """;

    private static final String SELECT_ALL_PLANETS = """
        SELECT * FROM planets
    """;

    private static final String INSERT_PLANET = """
        INSERT INTO planets (name, level, size, resource_rates, last_regeneration)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
        name = ?, level = ?, size = ?, resource_rates = ?, last_regeneration = ?
    """;

    private static final String UPDATE_PLANET = """
        UPDATE planets SET 
        name = ?, level = ?, size = ?, resource_rates = ?, last_regeneration = ?
        WHERE id = ?
    """;

    private static final String DELETE_PLANET = """
        DELETE FROM planets WHERE id = ?
    """;

    private final Gson gson;
    private final Type resourceRatesType;

    public PlanetDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        super(plugin, databaseManager);
        this.gson = new Gson();
        this.resourceRatesType = new TypeToken<Map<String, Double>>(){}.getType();
    }

    /**
     * Loads a planet from the database
     * @param id The planet ID
     * @return CompletableFuture containing the planet if found
     */
    public CompletableFuture<Optional<Planet>> loadPlanet(int id) {
        return executeQuery(
                SELECT_PLANET,
                stmt -> stmt.setInt(1, id),
                rs -> {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlanet(rs));
                    }
                    return Optional.empty();
                }
        );
    }

    /**
     * Loads all planets from the database
     * @return CompletableFuture containing list of all planets
     */
    public CompletableFuture<List<Planet>> loadAllPlanets() {
        return executeQuery(
                SELECT_ALL_PLANETS,
                stmt -> {},
                rs -> {
                    List<Planet> planets = new ArrayList<>();
                    while (rs.next()) {
                        planets.add(mapResultSetToPlanet(rs));
                    }
                    return planets;
                }
        );
    }

    /**
     * Saves a planet to the database
     * @param planet The planet to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> savePlanet(@NotNull Planet planet) {
        String resourceRatesJson = gson.toJson(planet.getResourceRates(), resourceRatesType);

        return executeUpdate(
                databaseManager.isUsingH2() ? UPDATE_PLANET : INSERT_PLANET,
                stmt -> {
                    int index = 1;

                    if (!databaseManager.isUsingH2()) {
                        // Insert parameters for MySQL
                        stmt.setString(index++, planet.getName());
                        stmt.setInt(index++, planet.getLevel());
                        stmt.setInt(index++, planet.getSize());
                        stmt.setString(index++, resourceRatesJson);
                        stmt.setTimestamp(index++, Timestamp.from(planet.getLastRegeneration()));
                    }

                    // Update parameters (used for both MySQL and H2)
                    stmt.setString(index++, planet.getName());
                    stmt.setInt(index++, planet.getLevel());
                    stmt.setInt(index++, planet.getSize());
                    stmt.setString(index++, resourceRatesJson);
                    stmt.setTimestamp(index++, Timestamp.from(planet.getLastRegeneration()));

                    if (databaseManager.isUsingH2()) {
                        // H2 needs the ID for the WHERE clause
                        stmt.setInt(index, planet.getId());
                    }
                }
        ).thenApply(result -> null);
    }

    /**
     * Deletes a planet from the database
     * @param id The ID of the planet to delete
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> deletePlanet(int id) {
        return executeUpdate(
                DELETE_PLANET,
                stmt -> stmt.setInt(1, id)
        ).thenApply(result -> null);
    }

    @SuppressWarnings("unchecked")
    private Planet mapResultSetToPlanet(ResultSet rs) throws Exception {
        String resourceRatesJson = rs.getString("resource_rates");
        Map<String, Double> resourceRates = resourceRatesJson != null ?
                gson.fromJson(resourceRatesJson, resourceRatesType) :
                new HashMap<>();

        return new Planet(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("level"),
                rs.getInt("size"),
                resourceRates,
                rs.getTimestamp("last_regeneration").toInstant()
        );
    }
}