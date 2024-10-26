package io.starseed.asteroidCore;

import io.starseed.asteroidCore.cache.CacheManager;
import io.starseed.asteroidCore.config.ConfigurationManager;
import io.starseed.asteroidCore.database.DatabaseManager;
import io.starseed.asteroidCore.events.EventManager;
import io.starseed.asteroidCore.modules.planet.PlanetModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AsteroidCore extends JavaPlugin {
    private static AsteroidCore instance;
    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private ConfigurationManager configManager;
    private EventManager eventManager;

    // Module managers
    private PlanetModule planetModule;
    private EconomyModule economyModule;
    private MiningModule miningModule;
    private PickaxeModule pickaxeModule;
    private PrivateMineModule privateMineModule;

    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();

        try {
            // Initialize core systems
            logger.info("§b[AsteroidCore] Initializing configuration...");
            this.configManager = new ConfigurationManager(this);

            logger.info("§b[AsteroidCore] Initializing database connection...");
            this.databaseManager = new DatabaseManager(this);

            logger.info("§b[AsteroidCore] Initializing cache system...");
            this.cacheManager = new CacheManager(this);

            logger.info("§b[AsteroidCore] Initializing event system...");
            this.eventManager = new EventManager(this);

            // Initialize modules
            initializeModules();

            logger.info("§a[AsteroidCore] Plugin has been successfully enabled!");
        } catch (Exception e) {
            logger.severe("§c[AsteroidCore] Failed to enable: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void initializeModules() {
        // Initialize modules in correct order based on dependencies
        this.planetModule = new PlanetModule(this);
        this.economyModule = new EconomyModule(this);
        this.miningModule = new MiningModule(this);
        this.pickaxeModule = new PickaxeModule(this);
        this.privateMineModule = new PrivateMineModule(this);

        // Enable modules
        planetModule.enable();
        economyModule.enable();
        miningModule.enable();
        pickaxeModule.enable();
        privateMineModule.enable();
    }

    @Override
    public void onDisable() {
        // Disable modules in reverse order
        if (privateMineModule != null) privateMineModule.disable();
        if (pickaxeModule != null) pickaxeModule.disable();
        if (miningModule != null) miningModule.disable();
        if (economyModule != null) economyModule.disable();
        if (planetModule != null) planetModule.disable();

        // Cleanup core systems
        if (cacheManager != null) cacheManager.shutdown();
        if (databaseManager != null) databaseManager.shutdown();

        getLogger().info("§c[AsteroidCore] Plugin has been disabled!");
        instance = null;
    }

    // Getters for managers and modules
    public static AsteroidCore getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public PlanetModule getPlanetModule() {
        return planetModule;
    }

    public EconomyModule getEconomyModule() {
        return economyModule;
    }

    public MiningModule getMiningModule() {
        return miningModule;
    }

    public PickaxeModule getPickaxeModule() {
        return pickaxeModule;
    }

    public PrivateMineModule getPrivateMineModule() {
        return privateMineModule;
    }
}