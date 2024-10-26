package io.starseed.commands;

import io.starseed.asteroidCore.AsteroidEx;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AsteroidExCommand extends Command implements TabExecutor {
    private final AsteroidEx plugin;

    public AsteroidExCommand(AsteroidEx plugin) {
        super("asteroidex");
        this.plugin = plugin;
        this.setDescription("Main command for AsteroidEx Prison");
        this.setUsage("/asteroidex <subcommand>");
        this.setAliases(Arrays.asList("aex", "prison"));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(sender);
                break;
            case "reload":
                if (!sender.hasPermission("asteroidex.admin.reload")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                reloadPlugin(sender);
                break;
            case "mine":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                handleMineCommand((Player) sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "pickaxe":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                handlePickaxeCommand((Player) sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /asteroidex help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AsteroidEx Prison Help ===");

        // Create clickable command components
        List<TextComponent> commands = new ArrayList<>();

        if (sender.hasPermission("asteroidex.admin")) {
            TextComponent adminCmd = new TextComponent(ChatColor.YELLOW + "/asteroidex reload " + ChatColor.GRAY + "- Reload the plugin");
            adminCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/asteroidex reload"));
            adminCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to reload the plugin").color(ChatColor.GRAY).create()));
            commands.add(adminCmd);
        }

        TextComponent mineCmd = new TextComponent(ChatColor.YELLOW + "/asteroidex mine " + ChatColor.GRAY + "- Mine management");
        mineCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/asteroidex mine "));
        mineCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to manage your mine").color(ChatColor.GRAY).create()));
        commands.add(mineCmd);

        TextComponent pickaxeCmd = new TextComponent(ChatColor.YELLOW + "/asteroidex pickaxe " + ChatColor.GRAY + "- Pickaxe management");
        pickaxeCmd.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/asteroidex pickaxe "));
        pickaxeCmd.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to manage your pickaxe").color(ChatColor.GRAY).create()));
        commands.add(pickaxeCmd);

        // Send all command components
        commands.forEach(sender::spigot);
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            plugin.getConfigManager().reloadConfigurations();
            sender.sendMessage(ChatColor.GREEN + "AsteroidEx has been reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload AsteroidEx: " + e.getMessage());
            plugin.getLogger().severe("Failed to reload plugin: " + e.getMessage());
        }
    }

    private void handleMineCommand(Player player, String[] args) {
        if (args.length == 0) {
            // Open mine management GUI
            plugin.getModuleManager().getMineModule().openMineGUI(player);
            return;
        }

        // Handle mine subcommands
        switch (args[0].toLowerCase()) {
            case "create":
                // Handle mine creation
                break;
            case "delete":
                // Handle mine deletion
                break;
            case "upgrade":
                // Handle mine upgrade
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown mine subcommand. Use /asteroidex mine for help.");
                break;
        }
    }

    private void handlePickaxeCommand(Player player, String[] args) {
        if (args.length == 0) {
            // Open pickaxe management GUI
            plugin.getModuleManager().getPickaxeModule().openPickaxeGUI(player);
            return;
        }

        // Handle pickaxe subcommands
        switch (args[0].toLowerCase()) {
            case "enchant":
                // Handle pickaxe enchanting
                break;
            case "upgrade":
                // Handle pickaxe upgrading
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown pickaxe subcommand. Use /asteroidex pickaxe for help.");
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main command completions
            List<String> commands = new ArrayList<>(Arrays.asList("mine", "pickaxe", "help"));
            if (sender.hasPermission("asteroidex.admin")) {
                commands.add("reload");
            }
            completions.addAll(commands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            // Subcommand completions
            switch (args[0].toLowerCase()) {
                case "mine":
                    completions.addAll(Arrays.asList("create", "delete", "upgrade"));
                    break;
                case "pickaxe":
                    completions.addAll(Arrays.asList("enchant", "upgrade"));
                    break;
            }
            completions = completions.stream()
                    .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}