package io.starseed.asteroidCore.modules.planet.handlers;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.modules.planet.models.PlanetStructure;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StructureHandler {
    private final AsteroidCore plugin;
    private final SchematicHandler schematicHandler;
    private final Map<String, StructureTemplate> structureTemplates;

    public StructureHandler(AsteroidCore plugin, SchematicHandler schematicHandler) {
        this.plugin = plugin;
        this.schematicHandler = schematicHandler;
        this.structureTemplates = new HashMap<>();
        loadStructureTemplates();
    }

    private void loadStructureTemplates() {
        FileConfiguration config = plugin.getConfigManager().getModuleConfig("structures").getConfig();
        ConfigurationSection structures = config.getConfigurationSection("structures");
        
        if (structures != null) {
            for (String key : structures.getKeys(false)) {
                ConfigurationSection structure = structures.getConfigurationSection(key);
                if (structure != null) {
                    StructureTemplate template = new StructureTemplate(
                        key,
                        structure.getString("schematic"),
                        structure.getInt("cost"),
                        structure.getInt("level_requirement"),
                        loadEffects(structure.getConfigurationSection("effects"))
                    );
                    structureTemplates.put(key, template);
                }
            }
        }
    }

    private Map<String, Double> loadEffects(ConfigurationSection effects) {
        Map<String, Double> effectMap = new HashMap<>();
        if (effects != null) {
            for (String key : effects.getKeys(false)) {
                effectMap.put(key, effects.getDouble(key));
            }
        }
        return effectMap;
    }

    public CompletableFuture<Boolean> placeStructure(String structureType, Location location, int rotation) {
        StructureTemplate template = structureTemplates.get(structureType);
        if (template == null) {
            return CompletableFuture.completedFuture(false);
        }

        return schematicHandler.pasteSchematic(template.getSchematicName(), location, rotation, true);
    }

    public boolean canPlaceStructure(String structureType, Location location, int planetLevel) {
        StructureTemplate template = structureTemplates.get(structureType);
        if (template == null || template.getLevelRequirement() > planetLevel) {
            return false;
        }

        // Check for nearby structures
        return !hasNearbyStructures(location, 10); // 10 block radius
    }

    private boolean hasNearbyStructures(Location location, int radius) {
        // Implementation for checking nearby structures
        // This would typically query from a database or cache of placed structures
        return false; // Placeholder
    }

    public Map<String, Double> getStructureEffects(String structureType) {
        StructureTemplate template = structureTemplates.get(structureType);
        return template != null ? new HashMap<>(template.getEffects()) : new HashMap<>();
    }

    private static class StructureTemplate {
        private final String type;
        private final String schematicName;
        private final int cost;
        private final int levelRequirement;
        private final Map<String, Double> effects;

        public StructureTemplate(String type, String schematicName, int cost, int levelRequirement, Map<String, Double> effects) {
            this.type = type;
            this.schematicName = schematicName;
            this.cost = cost;
            this.levelRequirement = levelRequirement;
            this.effects = effects;
        }

        public String getType() { return type; }
        public String getSchematicName() { return schematicName; }
        public int getCost() { return cost; }
        public int getLevelRequirement() { return levelRequirement; }
        public Map<String, Double> getEffects() { return new HashMap<>(effects); }
    }
}
