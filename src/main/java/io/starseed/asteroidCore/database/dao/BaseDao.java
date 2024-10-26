package io.starseed.asteroidCore.database.dao;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class BaseDao {
    protected final AsteroidCore plugin;
    protected final DatabaseManager databaseManager;

    public BaseDao(AsteroidCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * Executes a database query asynchronously
     * @param query The SQL query
     * @param paramSetter Function to set parameters on the PreparedStatement
     * @param resultMapper Function to map the ResultSet to the desired type
     * @param <T> The return type
     * @return CompletableFuture containing the query result
     */
    protected <T> CompletableFuture<T> executeQuery(String query,
                                                    ThrowingConsumer<PreparedStatement> paramSetter,
                                                    ThrowingFunction<ResultSet, T> resultMapper) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                paramSetter.accept(stmt);

                try (ResultSet rs = stmt.executeQuery()) {
                    return resultMapper.apply(rs);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database query failed: " + query, e);
                throw new RuntimeException("Database query failed", e);
            }
        });
    }

    /**
     * Executes a database update asynchronously
     * @param query The SQL query
     * @param paramSetter Function to set parameters on the PreparedStatement
     * @return CompletableFuture containing the number of affected rows
     */
    protected CompletableFuture<Integer> executeUpdate(String query,
                                                       ThrowingConsumer<PreparedStatement> paramSetter) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                paramSetter.accept(stmt);
                return stmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database update failed: " + query, e);
                throw new RuntimeException("Database update failed", e);
            }
        });
    }

    @FunctionalInterface
    protected interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
}