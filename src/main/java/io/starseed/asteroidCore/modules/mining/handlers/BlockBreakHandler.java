package io.starseed.asteroidCore.modules.mining.handlers;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.mining.MiningModule;
import io.starseed.asteroidCore.modules.mining.models.MiningStats;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockBreakHandler {
    private final AsteroidCore plugin;
    private final MiningModule miningModule;
    private final Map<Material, Double> blockValues;

    public BlockBreakHandler(AsteroidCore plugin, MiningModule miningModule) {
        this.plugin = plugin;
        this.miningModule = miningModule;
        this.blockValues = loadBlockValues();
    }

    private Map<Material, Double> loadBlockValues() {
        Map<Material, Double> values = new HashMap<>();
        plugin.getConfigManager().getModuleConfig("mining").getConfig()
            .getConfigurationSection("block_values").getKeys(false)
            .forEach(key -> {
                Material material = Material.valueOf(key.toUpperCase());
                double value = plugin.getConfigManager().getModuleConfig("mining")
                    .getConfig().getDouble("block_values." + key);
                values.put(material, value);
            });
        return values;
    }

    public boolean canBreakBlock(Player player, Block block) {
        // Check if player has permission
        if (!player.hasPermission("asteroidcore.mining.break")) {
            return false;
        }

        // Check if block is in a valid mining area
        return isValidMiningArea(block);
    }

    private boolean isValidMiningArea(Block block) {
        // Check if block is in a private mine or planet
        String worldName = block.getWorld().getName();
        return worldName.startsWith("pmine_") || worldName.startsWith("planet_");
    }

    public CompletableFuture<Double> handleBlockBreak(Player player, Block block) {
        return CompletableFuture.supplyAsync(() -> {
            MiningStats stats = miningModule.getPlayerStats(player.getUniqueId());
            
            // Update statistics
            stats.incrementBlocksBroken();
            stats.incrementBlockType(block.getType().name());
            
            // Calculate value
            double baseValue = blockValues.getOrDefault(block.getType(), 1.0);
            double multiplier = stats.calculateTotalMultiplier();
            double finalValue = baseValue * multiplier;
            
            // Add to total value
            stats.incrementTotalValue(finalValue);
            
            // Handle drops
            handleBlockDrops(player, block);
            
            return finalValue;
        });
    }

    private void handleBlockDrops(Player player, Block block) {
        // Custom drop handling logic here
        // For now, just give normal drops
        block.getDrops(player.getInventory().getItemInMainHand())
            .forEach(drop -> player.getInventory().addItem(drop));
    }

    public void reloadBlockValues() {
        blockValues.clear();
        blockValues.putAll(loadBlockValues());
    }
}
