package io.starseed.asteroidCore.modules.planet.listeners;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.planet.PlanetModule;
import io.starseed.asteroidCore.modules.planet.PlanetWorldManager;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.entity.Player;

public class PlanetListener implements Listener {
    private final AsteroidCore plugin;
    private final PlanetModule planetModule;
    private final PlanetWorldManager planetWorldManager;

    public PlanetListener(AsteroidCore plugin, PlanetModule planetModule) {
        this.plugin = plugin;
        this.planetModule = planetModule;
        this.planetWorldManager = planetWorldManager;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        World world = event.getLocation().getWorld();
        if (world != null && world.getName().startsWith("planet_")) {
            if (plugin.getConfigManager().getModuleConfig("planets")
                    .getConfig().getBoolean("protection.prevent_mob_spawning", true)) {
                if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();

        if (fromWorld != null && toWorld != null) {
            if (fromWorld.getName().startsWith("planet_") || toWorld.getName().startsWith("planet_")) {
                // Handle custom teleport effects and checks
                planetWorldManager.handleTeleport(event.getPlayer(), fromWorld, toWorld);
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        World world = event.getFrom().getWorld();
        if (world != null && world.getName().startsWith("planet_")) {
            // Prevent portal creation/usage in planet worlds
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        World world = event.getEntity().getWorld();
        if (world != null && world.getName().startsWith("planet_")) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                // Check PvP settings for the planet
                int planetId = Integer.parseInt(world.getName().split("_")[1]);
                if (!planetModule.isPvPEnabled(planetId)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        if (world.getName().startsWith("planet_")) {
            // Apply custom world settings when planet world is loaded
            planetWorldManager.setupPlanetWorld(world);
        }
    }
}