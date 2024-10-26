package io.starseed.asteroidCore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.database.dao.*;
import io.starseed.asteroidCore.database.schema.SchemaManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    private final AsteroidCore plugin;
    private HikariDataSource dataSource;
    private DatabaseType databaseType;
    private final SchemaManager schemaManager;

    // DAOs
    private final PlayerDao playerDao;
    private final StatisticsDao statisticsDao;
    private final PlanetDao planetDao;
    private final PrivateMineDao privateMineDao;
    private final PickaxeDao pickaxeDao;
    private final EnchantmentDao enchantmentDao;

    public enum DatabaseType {
        H2,
        MYSQL
    }

    public DatabaseManager(AsteroidCore plugin) {
        this.plugin = plugin;

        // Initialize connection
        initialize();

        // Initialize schema manager
        this.schemaManager = new SchemaManager(plugin, this);

        // Initialize DAOs
        this.playerDao = new PlayerDao(plugin, this);
        this.statisticsDao = new StatisticsDao(plugin, this);
        this.planetDao = new PlanetDao(plugin, this);
        this.privateMineDao = new PrivateMineDao(plugin, this);
        this.pickaxeDao = new PickaxeDao(plugin, this);
        this.enchantmentDao = new EnchantmentDao(plugin, this);

        // Create tables
        createTables();
    }

    private void initialize() {
        try {
            // Load database config
            File configFile = new File(plugin.getDataFolder(), "database.yml");
            if (!configFile.exists()) {
                plugin.saveResource("database.yml", false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            this.databaseType = DatabaseType.valueOf(config.getString("database.type", "H2").toUpperCase());

            // Initialize the connection pool
            HikariConfig hikariConfig = new HikariConfig();
            ConfigurationSection dbConfig;

            if (databaseType == DatabaseType.H2) {
                dbConfig = config.getConfigurationSection("database.h2");
                String dbFile = new File(plugin.getDataFolder(), dbConfig.getString("file", "database/asteroidcore"))
                        .getAbsolutePath();

                hikariConfig.setJdbcUrl("jdbc:h2:file:" + dbFile + ";MODE=MySQL");
                hikariConfig.setDriverClassName("org.h2.Driver");

                plugin.getLogger().info("§b[Database] Using H2 database at: " + dbFile);
            } else {
                dbConfig = config.getConfigurationSection("database.mysql");
                String host = dbConfig.getString("host", "localhost");
                int port = dbConfig.getInt("port", 3306);
                String database = dbConfig.getString("database", "asteroidcore");
                String username = dbConfig.getString("username", "root");
                String password = dbConfig.getString("password", "");

                hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
                hikariConfig.setUsername(username);
                hikariConfig.setPassword(password);
                hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

                plugin.getLogger().info("§b[Database] Using MySQL database at: " + host + ":" + port);
            }

            // Common configuration
            ConfigurationSection settings = config.getConfigurationSection("database.settings");
            hikariConfig.setPoolName("AsteroidCore-Pool");
            hikariConfig.setMaximumPoolSize(dbConfig.getInt("pool-size", 10));
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(dbConfig.getLong("timeout", 30000));
            hikariConfig.setIdleTimeout(settings.getLong("cleanup-interval", 10) * 60000);
            hikariConfig.setMaxLifetime(settings.getLong("max-lifetime", 30) * 60000);
            hikariConfig.setConnectionTestQuery("SELECT 1");

            // Performance optimizations
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

            // Initialize the datasource
            this.dataSource = new HikariDataSource(hikariConfig);

            // Test the connection
            try (Connection conn = dataSource.getConnection()) {
                plugin.getLogger().info("§a[Database] Successfully connected to database!");
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "§c[Database] Failed to initialize database connection!", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Creates all necessary database tables
     */
    private void createTables() {
        try {
            plugin.getLogger().info("§b[Database] Initializing database tables...");
            schemaManager.initializeTables();
            plugin.getLogger().info("§a[Database] Database tables initialized successfully!");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "§c[Database] Failed to initialize database tables!", e);
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    /**
     * Gets a connection from the pool
     * @return A database connection
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection is not available");
        }
        return dataSource.getConnection();
    }

    /**
     * Checks if the database connection is using H2
     * @return true if using H2, false if using MySQL
     */
    public boolean isUsingH2() {
        return databaseType == DatabaseType.H2;
    }

    /**
     * Gets the database type
     * @return The current database type
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Gets connection pool statistics
     * @return A string containing pool statistics
     */
    public String getPoolStatistics() {
        return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    // DAO Getters
    public PlayerDao getPlayerDao() { return playerDao; }
    public StatisticsDao getStatisticsDao() { return statisticsDao; }
    public PlanetDao getPlanetDao() { return planetDao; }
    public PrivateMineDao getPrivateMineDao() { return privateMineDao; }
    public PickaxeDao getPickaxeDao() { return pickaxeDao; }
    public EnchantmentDao getEnchantmentDao() { return enchantmentDao; }

    /**
     * Executes database maintenance tasks
     */
    public void performMaintenance() {
        if (isUsingH2()) {
            CompletableFuture.runAsync(() -> {
                try (Connection conn = getConnection()) {
                    conn.createStatement().execute("ANALYZE");
                    conn.createStatement().execute("CHECKPOINT");
                    plugin.getLogger().info("§a[Database] Maintenance tasks completed successfully");
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "§c[Database] Failed to perform maintenance tasks", e);
                }
            });
        }
    }

    /**
     * Closes the database connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            plugin.getLogger().info("§b[Database] Shutting down database connections...");
            performMaintenance();
            dataSource.close();
            plugin.getLogger().info("§a[Database] Database connections closed successfully");
        }
    }
}