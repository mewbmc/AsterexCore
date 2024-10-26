package io.starseed.asteroidCore.modules.mining.listeners;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.mining.MiningModule;
import io.starseed.asteroidCore.modules.mining.handlers.BlockBreakHandler;
import io.starseed.asteroidCore.modules.mining.handlers.RewardHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {
    private final AsteroidCore plugin;
    private final MiningModule miningModule;
    private final BlockBreakHandler blockBreakHandler;
    private final RewardHandler rewardHandler;

    public MiningListener(AsteroidCore plugin, MiningModule miningModule) {
        this.plugin = plugin;
        this.miningModule = miningModule;
        this.blockBreakHandler = miningModule.getBlockBreakHandler();
        this.rewardHandler = miningModule.getRewardHandler();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (blockBreakHandler.canBreakBlock(player, event.getBlock())) {
            blockBreakHandler.handleBlockBreak(player, event.getBlock())
                .thenAccept(rewardHandler::giveReward);
        }
    }
}
