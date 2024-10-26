package io.starseed.asteroidCore.modules.mining.handlers;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.models.PrivateMine;
import io.starseed.asteroidCore.modules.mining.PrivateMineModule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateMineHandler {
    private final AsteroidCore plugin;
    private final PrivateMineModule module;
    private final Map<UUID, PrivateMine> loadedMines;
    private final Map<UUID, Long> lastRegeneration;

    public PrivateMineHandler(AsteroidCore plugin, PrivateMineModule module) {
        this.plugin = plugin;
        this.module = module;
        this.loadedMines = new ConcurrentHashMap<>();
        this.lastRegeneration = new HashMap<>();
    }

    public CompletableFuture<PrivateMine> createMine(Player owner) {
        return CompletableFuture.supplyAsync(() -> {
            PrivateMine mine = new PrivateMine();
            mine.setOwner(owner.getUniqueId());
            mine.setSize(25); // Default size
            mine.setTaxRate(0.1); // 10% default tax
            
            // Generate mine world
            String worldName = "pmine_" + owner.getUniqueId().toString().substring(0, 8);
            World world = plugin.getServer().createWorld(new org.bukkit.WorldCreator(worldName));
            mine.setWorld(world.getName());
            
            // Save to database
            plugin.getDatabaseManager().getPrivateMineDao().saveMine(mine);
            
            // Cache the mine
            loadedMines.put(owner.getUniqueId(), mine);
            
            return mine;
        });
    }

    public CompletableFuture<Void> regenerateMine(PrivateMine mine) {
        return CompletableFuture.runAsync(() -> {
            World world = plugin.getServer().getWorld(mine.getWorld());
            if (world == null) return;

            // Check cooldown
            long now = System.currentTimeMillis();
            long lastRegen = lastRegeneration.getOrDefault(mine.getOwner(), 0L);
            if (now - lastRegen < 300000) { // 5 minutes cooldown
                return;
            }

            // Regenerate blocks
            int size = mine.getSize();
            for (int x = -size; x <= size; x++) {
                for (int y = -size; y <= size; y++) {
                    for (int z = -size; z <= size; z++) {
                        Location loc = new Location(world, x, y, z);
                        // Set blocks based on mine configuration
                        // Implementation details here
                    }
                }
            }

            // Update last regeneration time
            lastRegeneration.put(mine.getOwner(), now);
        });
    }

    public boolean canMine(Player player, Location location) {
        PrivateMine mine = getMineAtLocation(location);
        if (mine == null) return false;

        return mine.getOwner().equals(player.getUniqueId()) || 
               mine.isWhitelisted(player.getUniqueId());
    }

    public PrivateMine getMineAtLocation(Location location) {
        String worldName = location.getWorld().getName();
        return loadedMines.values().stream()
                .filter(mine -> mine.getWorld().equals(worldName))
                .findFirst()
                .orElse(null);
    }

    public void unloadMine(UUID owner) {
        PrivateMine mine = loadedMines.remove(owner);
        if (mine != null) {
            // Save mine data
            plugin.getDatabaseManager().getPrivateMineDao().saveMine(mine);
            
            // Unload world
            World world = plugin.getServer().getWorld(mine.getWorld());
            if (world != null) {
                plugin.getServer().unloadWorld(world, true);
            }
        }
    }

    public Map<UUID, PrivateMine> getLoadedMines() {
        return new HashMap<>(loadedMines);
    }
}
