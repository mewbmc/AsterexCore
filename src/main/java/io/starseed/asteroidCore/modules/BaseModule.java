package io.starseed.asteroidCore.modules;

import io.starseed.asteroidCore.AsteroidCore;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class BaseModule {
    protected final AsteroidCore plugin;
    protected FileConfiguration config;
    protected boolean enabled = false;

    public BaseModule(AsteroidCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when the module is being enabled
     */
    public abstract void enable();

    /**
     * Called when the module is being disabled
     */
    public abstract void disable();

    /**
     * Loads or reloads the module's configuration
     */
    public abstract void reloadConfig();

    /**
     * @return whether the module is currently enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the plugin instance
     * @return the main plugin instance
     */
    protected AsteroidCore getPlugin() {
        return plugin;
    }
}