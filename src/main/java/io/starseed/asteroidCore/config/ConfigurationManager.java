package io.starseed.asteroidCore.config;

import io.starseed.asteroidCore.AsteroidCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigurationManager {
    private final AsteroidCore plugin;
    private final Map<String, ModuleConfig> moduleConfigs;
    private FileConfiguration mainConfig;

    public ConfigurationManager(AsteroidCore plugin) {
        this.plugin = plugin;
        this.moduleConfigs = new HashMap<>();
        loadMainConfig();
    }

    /**
     * Loads or reloads the main configuration file
     */
    public void loadMainConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.mainConfig = plugin.getConfig();
    }

    /**
     * Gets or creates a module configuration
     * @param moduleName Name of the module
     * @return ModuleConfig instance
     */
    public ModuleConfig getModuleConfig(String moduleName) {
        return moduleConfigs.computeIfAbsent(moduleName, name -> {
            File configFile = new File(plugin.getDataFolder() + "/modules", name + ".yml");
            return new ModuleConfig(plugin, configFile);
        });
    }

    /**
     * Reloads all configurations
     */
    public void reloadAll() {
        loadMainConfig();
        moduleConfigs.values().forEach(ModuleConfig::reload);
        plugin.getLogger().info("§a[Config] All configurations have been reloaded");
    }

    /**
     * Gets the main configuration
     * @return The main FileConfiguration
     */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    /**
     * Module-specific configuration class
     */
    public static class ModuleConfig {
        private final AsteroidCore plugin;
        private final File configFile;
        private FileConfiguration config;

        public ModuleConfig(AsteroidCore plugin, File configFile) {
            this.plugin = plugin;
            this.configFile = configFile;
            reload();
        }

        /**
         * Reloads the configuration from disk
         */
        public void reload() {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                plugin.saveResource("modules/" + configFile.getName(), false);
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
        }

        /**
         * Saves the configuration to disk
         */
        public void save() {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
            }
        }

        public FileConfiguration getConfig() {
            return config;
        }

        public ConfigurationSection getSection(String path) {
            return config.getConfigurationSection(path);
        }

        public void set(String path, Object value) {
            config.set(path, value);
        }
    }
}