package io.starseed.asteroidCore.database.schema;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SchemaManager {
    private final AsteroidCore plugin;
    private final DatabaseManager databaseManager;

    public SchemaManager(AsteroidCore plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void initializeTables() {
        plugin.getLogger().info("§b[Database] Initializing database tables...");

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Players Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    balance DECIMAL(15,2) DEFAULT 0,
                    tokens BIGINT DEFAULT 0,
                    prestige_level INT DEFAULT 0,
                    rank VARCHAR(32) DEFAULT 'DEFAULT'
                )
            """);

            // Player Statistics Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_statistics (
                    uuid VARCHAR(36) PRIMARY KEY,
                    blocks_mined BIGINT DEFAULT 0,
                    time_played BIGINT DEFAULT 0,
                    tokens_earned BIGINT DEFAULT 0,
                    money_earned DECIMAL(15,2) DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
            """);

            // Planets Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS planets (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(64) UNIQUE NOT NULL,
                    level INT DEFAULT 1,
                    size INT DEFAULT 100,
                    resource_rates TEXT,
                    last_regeneration TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Private Mines Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS private_mines (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    owner_uuid VARCHAR(36) NOT NULL,
                    name VARCHAR(64) NOT NULL,
                    size INT DEFAULT 25,
                    level INT DEFAULT 1,
                    is_public BOOLEAN DEFAULT FALSE,
                    resource_rates TEXT,
                    whitelist TEXT,
                    last_regeneration TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
            """);

            // Pickaxes Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pickaxes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    owner_uuid VARCHAR(36) NOT NULL,
                    name VARCHAR(64),
                    level INT DEFAULT 1,
                    experience BIGINT DEFAULT 0,
                    enchantments TEXT,
                    skin VARCHAR(64) DEFAULT 'DEFAULT',
                    crystals TEXT,
                    FOREIGN KEY (owner_uuid) REFERENCES players(uuid) ON DELETE CASCADE
                )
            """);

            plugin.getLogger().info("§a[Database] Successfully initialized all database tables!");

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "§c[Database] Failed to initialize database tables!", e);
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }
}