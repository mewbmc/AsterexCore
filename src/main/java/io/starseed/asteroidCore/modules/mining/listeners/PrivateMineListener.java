package io.starseed.asteroidCore.modules.mining.listeners;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.mining.PrivateMineModule;
import io.starseed.asteroidCore.modules.mining.handlers.PrivateMineHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PrivateMineListener implements Listener {
    private final AsteroidCore plugin;
    private final PrivateMineModule module;
    private final PrivateMineHandler handler;

    public PrivateMineListener(AsteroidCore plugin, PrivateMineModule module) {
        this.plugin = plugin;
        this.module = module;
        this.handler = module.getPrivateMineHandler();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!handler.canMine(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou don't have permission to mine here!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseManager().getPrivateMineDao().loadMine(player.getUniqueId())
            .thenAccept(mine -> {
                if (mine != null) {
                    // Load mine data
                    handler.getLoadedMines().put(player.getUniqueId(), mine);
                }
            });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handler.unloadMine(event.getPlayer().getUniqueId());
    }
}
