package io.starseed.asteroidCore.modules.planet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.models.Planet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlanetWorldManager {
    private final AsteroidCore plugin;
    private final PlanetModule planetModule;
    private final Cache<Integer, Location> spawnLocations;

    public PlanetWorldManager(AsteroidCore plugin, PlanetModule planetModule) {
        this.plugin = plugin;
        this.planetModule = planetModule;
        this.spawnLocations = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Generates a new world for a planet
     * @param planet The planet to generate a world for
     * @return CompletableFuture for completion
     */
    public CompletableFuture<World> generateWorld(Planet planet) {
        return CompletableFuture.supplyAsync(() -> {
            String worldName = "planet_" + planet.getId();
            WorldCreator creator = new WorldCreator(worldName)
                    .generator(new VoidChunkGenerator())
                    .environment(World.Environment.NORMAL)
                    .generateStructures(false);

            // Create and load the world
            World world = Bukkit.createWorld(creator);
            if (world == null) {
                throw new RuntimeException("Failed to create world for planet " + planet.getId());
            }

            // Setup world
            setupPlanetWorld(world);

            // Generate planet structure
            generatePlanetStructure(world, planet);

            return world;
        });
    }

    /**
     * Sets up a planet world with appropriate settings
     * @param world The world to setup
     */
    public void setupPlanetWorld(World world) {
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(true);

        // Set game rules
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);

        // Set world settings
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);

        // Set world border
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000); // Adjust based on planet size
    }

    /**
     * Generates the physical planet structure
     * @param world The world to generate in
     * @param planet The planet configuration
     */
    private void generatePlanetStructure(World world, Planet planet) {
        int size = planet.getSize();
        Map<String, Double> rates = planet.getResourceRates();

        // Calculate total weight for resource distribution
        double totalWeight = rates.values().stream().mapToDouble(Double::doubleValue).sum();
        Random random = new Random();

        // Generate the sphere
        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                for (int z = -size; z <= size; z++) {
                    // Check if point is within sphere
                    if (x*x + y*y + z*z <= size*size) {
                        Location loc = new Location(world, x, y, z);
                        Block block = loc.getBlock();

                        // Determine block type based on rates
                        double rand = random.nextDouble() * totalWeight;
                        double currentWeight = 0;

                        for (Map.Entry<String, Double> entry : rates.entrySet()) {
                            currentWeight += entry.getValue();
                            if (rand <= currentWeight) {
                                block.setType(Material.valueOf(entry.getKey()));
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Create spawn platform
        generateSpawnPlatform(world, size);
    }

    /**
     * Generates a spawn platform for the planet
     * @param world The world to generate in
     * @param size The planet size
     */
    private void generateSpawnPlatform(World world, int size) {
        Location spawnLoc = new Location(world, 0, size + 3, 0);

        // Create platform
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Location platformLoc = spawnLoc.clone().add(x, 0, z);
                platformLoc.getBlock().setType(Material.OBSIDIAN);
            }
        }

        // Set world spawn
        world.setSpawnLocation(spawnLoc);
        spawnLocations.put(Integer.parseInt(world.getName().split("_")[1]), spawnLoc);
    }

    /**
     * Gets the spawn location for a planet
     * @param planetId The planet ID
     * @return The spawn location
     */
    public Location getSpawnLocation(int planetId) {
        Location cached = spawnLocations.getIfPresent(planetId);
        if (cached != null) {
            return cached;
        }

        World world = Bukkit.getWorld("planet_" + planetId);
        if (world != null) {
            Location spawn = world.getSpawnLocation();
            spawnLocations.put(planetId, spawn);
            return spawn;
        }

        return null;
    }

    /**
     * Custom void chunk generator for planet worlds
     */
    private static class VoidChunkGenerator extends ChunkGenerator {
        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // Generate empty chunks
        }

        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // No surface generation
        }

        @Override
        public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // No bedrock generation
        }

        @Override
        public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            // No cave generation
        }

        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }

        @Override
        public boolean shouldGenerateSurface() {
            return false;
        }

        @Override
        public boolean shouldGenerateBedrock() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }
    }
}