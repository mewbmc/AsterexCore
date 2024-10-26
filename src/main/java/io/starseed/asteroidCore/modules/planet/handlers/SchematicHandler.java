package io.starseed.asteroidCore.modules.planet.handlers;


import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.starseed.asteroidCore.AsteroidCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SchematicHandler {
    private final AsteroidCore plugin;
    private final Map<String, Clipboard> schematicCache;
    private final File schematicFolder;

    public SchematicHandler(AsteroidCore plugin) {
        this.plugin = plugin;
        this.schematicCache = new HashMap<>();
        this.schematicFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicFolder.exists()) {
            schematicFolder.mkdirs();
            // Copy default schematics from resources
            plugin.saveResource("schematics/starter_planet.schem", false);
            plugin.saveResource("schematics/mining_facility.schem", false);
            plugin.saveResource("schematics/processing_plant.schem", false);
            plugin.saveResource("schematics/energy_core.schem", false);
        }
    }

    /**
     * Loads all schematics from the schematics folder into memory
     */
    public void loadSchematics() {
        File[] files = schematicFolder.listFiles((dir, name) ->
                name.endsWith(".schem") || name.endsWith(".schematic"));

        if (files != null) {
            for (File file : files) {
                try {
                    ClipboardFormat format = ClipboardFormats.findByFile(file);
                    if (format != null) {
                        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                            Clipboard clipboard = reader.read();
                            schematicCache.put(file.getName(), clipboard);
                            plugin.getLogger().info("§a[Schematics] Loaded: " + file.getName());
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("§c[Schematics] Failed to load: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Pastes a schematic asynchronously
     * @param schematicName Name of the schematic file
     * @param location Location to paste at
     * @param rotation Rotation in degrees
     * @param ignoreAir Whether to ignore air blocks
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Boolean> pasteSchematic(String schematicName, Location location, int rotation, boolean ignoreAir) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Clipboard clipboard = schematicCache.get(schematicName);
                if (clipboard == null) {
                    plugin.getLogger().warning("§c[Schematics] Schematic not found: " + schematicName);
                    future.complete(false);
                    return;
                }

                World world = location.getWorld();
                if (world == null) {
                    future.complete(false);
                    return;
                }

                // Create transform for rotation
                AffineTransform transform = new AffineTransform();
                transform = transform.rotateY(rotation);

                // Create edit session with FAWE
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(transform);

                    Operation operation = holder
                            .createPaste(editSession)
                            .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                            .ignoreAirBlocks(ignoreAir)
                            .build();

                    Operations.complete(operation);

                    future.complete(true);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("§c[Schematics] Failed to paste: " + schematicName);
                e.printStackTrace();
                future.complete(false);
            }
        });

        return future;
    }

    /**
     * Gets the dimensions of a loaded schematic
     * @param schematicName Name of the schematic
     * @return BlockVector3 containing dimensions, or null if not found
     */
    public BlockVector3 getSchematicDimensions(String schematicName) {
        Clipboard clipboard = schematicCache.get(schematicName);
        return clipboard != null ? clipboard.getDimensions() : null;
    }

    /**
     * Checks if a schematic is loaded
     * @param schematicName Name of the schematic
     * @return true if loaded
     */
    public boolean isSchematicLoaded(String schematicName) {
        return schematicCache.containsKey(schematicName);
    }

    /**
     * Reloads all schematics from disk
     */
    public void reloadSchematics() {
        schematicCache.clear();
        loadSchematics();
    }

    /**
     * Gets the center point offset of a schematic
     * @param schematicName Name of the schematic
     * @return BlockVector3 containing the offset, or null if not found
     */
    public BlockVector3 getSchematicOffset(String schematicName) {
        Clipboard clipboard = schematicCache.get(schematicName);
        return clipboard != null ? clipboard.getOrigin() : null;
    }
}