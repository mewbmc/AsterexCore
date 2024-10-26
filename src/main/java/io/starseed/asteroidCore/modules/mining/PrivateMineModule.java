package io.starseed.asteroidCore.modules.mining;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.BaseModule;
import io.starseed.asteroidCore.modules.mining.handlers.PrivateMineHandler;
import io.starseed.asteroidCore.modules.mining.listeners.PrivateMineListener;
import org.bukkit.configuration.file.FileConfiguration;

public class PrivateMineModule extends BaseModule {
    private PrivateMineHandler privateMineHandler;
    private FileConfiguration config;

    public PrivateMineModule(AsteroidCore plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // Load configuration
        this.config = plugin.getConfigManager().getModuleConfig("private_mine").getConfig();

        // Initialize handlers
        this.privateMineHandler = new PrivateMineHandler(plugin, this);

        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(
            new PrivateMineListener(plugin, this), plugin);

        this.enabled = true;
        plugin.getLogger().info("§a[Private Mine] Module enabled successfully!");
    }

    @Override
    public void disable() {
        this.enabled = false;
        plugin.getLogger().info("§c[Private Mine] Module disabled!");
    }

    @Override
    public void reloadConfig() {
        plugin.getConfigManager().getModuleConfig("private_mine").reload();
        this.config = plugin.getConfigManager().getModuleConfig("private_mine").getConfig();
    }

    public PrivateMineHandler getPrivateMineHandler() {
        return privateMineHandler;
    }
}
