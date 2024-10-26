package io.starseed.asteroidCore.modules.planet;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.config.ConfigurationManager;
import io.starseed.asteroidCore.models.Planet;
import io.starseed.asteroidCore.modules.BaseModule;
import io.starseed.asteroidCore.modules.planet.commands.PlanetCommand;
import io.starseed.asteroidCore.modules.planet.listeners.PlanetListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlanetModule extends BaseModule {
    private final Map<Integer, Planet> loadedPlanets;
    private final Map<String, Map<String, Double>> defaultResourceRates;
    private final Random random;
    private FileConfiguration config;

    public PlanetModule(AsteroidCore plugin) {
        super(plugin);
        this.loadedPlanets = new ConcurrentHashMap<>();
        this.defaultResourceRates = new HashMap<>();
        this.random = new Random();
    }

    @Override
    public void enable() {
        // Load configuration
        ConfigurationManager.ModuleConfig moduleConfig = plugin.getConfigManager().getModuleConfig("planets");
        this.config = moduleConfig.getConfig();

        // Load default resource rates
        loadDefaultResourceRates();

        // Load all planets from database
        loadAllPlanets();

        // Register listeners
        plugin.getServer().getPluginManager().registerEvents(new PlanetListener(plugin, this), plugin);

        // Register commands
        Objects.requireNonNull(plugin.getCommand("planet")).setExecutor(new PlanetCommand(plugin, this));

        this.enabled = true;
        plugin.getLogger().info("§a[Planets] Module enabled successfully!");
    }

    @Override
    public void disable() {
        // Save all planets
        saveAllPlanets();

        this.enabled = false;
        plugin.getLogger().info("§c[Planets] Module disabled!");
    }

    @Override
    public void reloadConfig() {
        plugin.getConfigManager().getModuleConfig("planets").reload();
        this.config = plugin.getConfigManager().getModuleConfig("planets").getConfig();
        loadDefaultResourceRates();
    }

    private void loadDefaultResourceRates() {
        defaultResourceRates.clear();
        ConfigurationSection ratesSection = config.getConfigurationSection("resource_rates");
        if (ratesSection != null) {
            for (String level : ratesSection.getKeys(false)) {
                ConfigurationSection levelSection = ratesSection.getConfigurationSection(level);
                Map<String, Double> rates = new HashMap<>();

                assert levelSection != null;
                for (String material : levelSection.getKeys(false)) {
                    rates.put(material, levelSection.getDouble(material));
                }

                defaultResourceRates.put(level, rates);
            }
        }
    }

    private void loadAllPlanets() {
        plugin.getDatabaseManager().getPlanetDao().loadAllPlanets()
                .thenAccept(planets -> {
                    loadedPlanets.clear();
                    planets.forEach(planet -> loadedPlanets.put(planet.getId(), planet));
                    plugin.getLogger().info("§b[Planets] Loaded " + planets.size() + " planets");
                })
                .exceptionally(throwable -> {
                    plugin.getLogger().severe("§c[Planets] Failed to load planets: " + throwable.getMessage());
                    return null;
                });
    }

    private void saveAllPlanets() {
        loadedPlanets.values().forEach(planet ->
                plugin.getDatabaseManager().getPlanetDao().savePlanet(planet)
                        .exceptionally(throwable -> {
                            plugin.getLogger().severe("§c[Planets] Failed to save planet " + planet.getId() + ": " + throwable.getMessage());
                            return null;
                        })
        );
    }

    /**
     * Creates a new planet
     * @param name Planet name
     * @param level Initial level
     * @return CompletableFuture containing the created planet
     */
    public CompletableFuture<Planet> createPlanet(String name, int level) {
        // Create planet instance
        Planet planet = new Planet(-1, name);
        planet.setLevel(level);
        planet.setSize(config.getInt("default_size", 100));

        // Set default resource rates for level
        Map<String, Double> rates = defaultResourceRates.get("level_" + level);
        if (rates != null) {
            planet.setResourceRates(new HashMap<>(rates));
        }

        // Save to database
        return plugin.getDatabaseManager().getPlanetDao().savePlanet(planet)
                .thenApply(v -> {
                    loadedPlanets.put(planet.getId(), planet);
                    return planet;
                });
    }

    /**
     * Gets a planet by ID
     * @param id Planet ID
     * @return Optional containing the planet if found
     */
    public Optional<Planet> getPlanet(int id) {
        return Optional.ofNullable(loadedPlanets.get(id));
    }

    /**
     * Regenerates a planet's resources
     * @param planet The planet to regenerate
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> regeneratePlanet(Planet planet) {
        return CompletableFuture.runAsync(() -> {
            // Get the planet's world
            World world = plugin.getServer().getWorld("planet_" + planet.getId());
            if (world == null) return;

            int size = planet.getSize();
            Map<String, Double> rates = planet.getResourceRates();

            // Calculate total weight
            double totalWeight = rates.values().stream().mapToDouble(Double::doubleValue).sum();

            // Regenerate blocks
            for (int x = -size; x <= size; x++) {
                for (int y = -size; y <= size; y++) {
                    for (int z = -size; z <= size; z++) {
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

            // Update last regeneration time
            planet.setLastRegeneration(java.time.Instant.now());
            plugin.getDatabaseManager().getPlanetDao().savePlanet(planet);
        });
    }
}