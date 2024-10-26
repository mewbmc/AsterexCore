package io.starseed.asteroidCore.modules.planet.commands;

import io.starseed.asteroidCore.AsteroidCore;
import io.starseed.asteroidCore.models.Planet;
import io.starseed.asteroidCore.modules.planet.PlanetModule;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanetCommand implements CommandExecutor, TabCompleter {
    private final AsteroidCore plugin;
    private final PlanetModule planetModule;

    public PlanetCommand(AsteroidCore plugin, PlanetModule planetModule) {
        this.plugin = plugin;
        this.planetModule = planetModule;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "regen":
            case "regenerate":
                return handleRegenerate(sender, args);
            case "upgrade":
                return handleUpgrade(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "list":
                return handleList(sender);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("asteroidcore.planet.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to create planets!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /planet create <name>");
            return true;
        }

        String name = args[1];
        planetModule.createPlanet(name, 1)
                .thenAccept(planet -> {
                    sender.sendMessage(ChatColor.GREEN + "Planet " + name + " created successfully!");
                    planetModule.generateWorld(planet)
                            .thenRun(() -> sender.sendMessage(ChatColor.GREEN + "World generated successfully!"))
                            .exceptionally(throwable -> {
                                sender.sendMessage(ChatColor.RED + "Failed to generate world: " + throwable.getMessage());
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    sender.sendMessage(ChatColor.RED + "Failed to create planet: " + throwable.getMessage());
                    return null;
                });

        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can teleport to planets!");
            return true;
        }

        if (!sender.hasPermission("asteroidcore.planet.teleport")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to teleport to planets!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /planet tp <planetId>");
            return true;
        }

        try {
            int planetId = Integer.parseInt(args[1]);
            Optional<Planet> planet = planetModule.getPlanet(planetId);

            if (planet.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Planet not found!");
                return true;
            }

            planetModule.teleportToSpawn(player, planet.get())
                    .thenRun(() -> sender.sendMessage(ChatColor.GREEN + "Teleported to planet " + planet.get().getName()))
                    .exceptionally(throwable -> {
                        sender.sendMessage(ChatColor.RED + "Failed to teleport: " + throwable.getMessage());
                        return null;
                    });

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid planet ID!");
        }

        return true;
    }

    private boolean handleRegenerate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("asteroidcore.planet.regenerate")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to regenerate planets!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /planet regen <planetId>");
            return true;
        }

        try {
            int planetId = Integer.parseInt(args[1]);
            Optional<Planet> planet = planetModule.getPlanet(planetId);

            if (planet.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Planet not found!");
                return true;
            }

            planetModule.regeneratePlanet(planet.get())
                    .thenRun(() -> sender.sendMessage(ChatColor.GREEN + "Planet regenerated successfully!"))
                    .exceptionally(throwable -> {
                        sender.sendMessage(ChatColor.RED + "Failed to regenerate planet: " + throwable.getMessage());
                        return null;
                    });

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid planet ID!");
        }

        return true;
    }

    private boolean handleUpgrade(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can upgrade planets!");
            return true;
        }

        if (!sender.hasPermission("asteroidcore.planet.upgrade")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to upgrade planets!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /planet upgrade <planetId>");
            return true;
        }

        try {
            int planetId = Integer.parseInt(args[1]);
            planetModule.upgradePlanet(player, planetId)
                    .thenAccept(success -> {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Planet upgraded successfully!");
                        }
                    })
                    .exceptionally(throwable -> {
                        sender.sendMessage(ChatColor.RED + "Failed to upgrade planet: " + throwable.getMessage());
                        return null;
                    });

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid planet ID!");
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("asteroidcore.planet.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to view planet info!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /planet info <planetId>");
            return true;
        }

        try {
            int planetId = Integer.parseInt(args[1]);
            Optional<Planet> planet = planetModule.getPlanet(planetId);

            if (planet.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Planet not found!");
                return true;
            }

            // Send planet information
            Planet p = planet.get();
            sender.sendMessage(ChatColor.GOLD + "=== Planet Information ===");
            sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + p.getId());
            sender.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + p.getName());
            sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + p.getLevel());
            sender.sendMessage(ChatColor.YELLOW + "Size: " + ChatColor.WHITE + p.getSize());
            sender.sendMessage(ChatColor.YELLOW + "Resource Rates:");
            p.getResourceRates().forEach((material, rate) ->
                    sender.sendMessage(ChatColor.GRAY + "  - " + material + ": " + rate + "%"));

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid planet ID!");
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("asteroidcore.planet.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to list planets!");
            return true;
        }

        List<Planet> planets = planetModule.getAllPlanets();
        sender.sendMessage(ChatColor.GOLD + "=== Planets ===");
        planets.forEach(p -> sender.sendMessage(
                ChatColor.YELLOW + "ID: " + p.getId() +
                        ChatColor.WHITE + " - " +
                        ChatColor.YELLOW + "Name: " + p.getName() +
                        ChatColor.WHITE + " - " +
                        ChatColor.YELLOW + "Level: " + p.getLevel()
        ));

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Planet Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/planet create <name>" + ChatColor.WHITE + " - Create a new planet");
        sender.sendMessage(ChatColor.YELLOW + "/planet tp <planetId>" + ChatColor.WHITE + " - Teleport to a planet");
        sender.sendMessage(ChatColor.YELLOW + "/planet regen <planetId>" + ChatColor.WHITE + " - Regenerate a planet");
        sender.sendMessage(ChatColor.YELLOW + "/planet upgrade <planetId>" + ChatColor.WHITE + " - Upgrade a planet");
        sender.sendMessage(ChatColor.YELLOW + "/planet info <planetId>" + ChatColor.WHITE + " - View planet information");
        sender.sendMessage(ChatColor.YELLOW + "/planet list" + ChatColor.WHITE + " - List all planets");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("asteroidcore.planet.create")) subcommands.add("create");
            if (sender.hasPermission("asteroidcore.planet.teleport")) subcommands.add("tp");
            if (sender.hasPermission("asteroidcore.planet.regenerate")) subcommands.add("regen");
            if (sender.hasPermission("asteroidcore.planet.upgrade")) subcommands.add("upgrade");
            if (sender.hasPermission("asteroidcore.planet.info")) subcommands.add("info");
            if (sender.hasPermission("asteroidcore.planet.list")) subcommands.add("list");

            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "tp":
                case "regen":
                case "upgrade":
                case "info":
                    // Return list of planet IDs
                    return planetModule.getAllPlanets().stream()
                            .map(p -> String.valueOf(p.getId()))
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
            }
        }

        return completions;
    }
}