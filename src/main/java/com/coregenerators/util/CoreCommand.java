package com.coregenerators.util;

import com.coregenerators.main.CoreGenerators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CoreCommand implements CommandExecutor, TabCompleter {

    private final CoreGenerators plugin;

    public CoreCommand(CoreGenerators plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eVerwende: /coregen reload, give, remove, debug");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!hasPermission(sender, Permissions.ADMIN_RELOAD)) return true;

                plugin.reloadConfig();
                Messages.load();
                sender.sendMessage(Messages.get("reloaded"));
            }

            case "give" -> {
                if (!hasPermission(sender, Permissions.ADMIN_GIVE)) return true;

                if (args.length < 3) {
                    sender.sendMessage("§cVerwendung: /coregen give <Spieler> <GeneratorID> [Level]");
                    return true;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Messages.get("player-not-found"));
                    return true;
                }

                String generatorId = args[2];
                int level = 0;
                if (args.length >= 4) {
                    try {
                        level = Integer.parseInt(args[3]);
                    } catch (NumberFormatException ignored) {
                        sender.sendMessage("§cUngültiges Level, Standardwert 0 wird verwendet.");
                    }
                }

                target.getInventory().addItem(plugin.getStorage().createGeneratorItem(generatorId, level));
                sender.sendMessage(Messages.get("generator-given", "%player%", target.getName()));
            }

            case "remove" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
                    return true;
                }

                if (!hasPermission(player, Permissions.ADMIN_REMOVE)) return true;

                plugin.getStorage().removeAllGenerators(player.getUniqueId());
                player.sendMessage(Messages.get("removed"));
            }

            case "debug" -> {
                boolean current = plugin.getConfig().getBoolean("debug.tick-logging", false);
                boolean newValue = !current;

                plugin.getConfig().set("debug.tick-logging", newValue);
                plugin.saveConfig();

                sender.sendMessage("§eTick-Debug ist jetzt " + (newValue ? "§aaktiviert§e." : "§cdeaktiviert§e."));
            }

            default -> sender.sendMessage("§cUnbekannter Unterbefehl.");
        }

        return true;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(Messages.get("no-permission"));
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "give", "remove", "debug");
            return StringUtil.copyPartialMatches(args[0], subcommands, new ArrayList<>());
        }

        // Tab für: /coregen give <Spieler>
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> playerNames = plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
            return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
        }

        // Tab für: /coregen give <Spieler> <GeneratorID>
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> generatorIds = new ArrayList<>(CoreGenerators.generators.keySet());
            return StringUtil.copyPartialMatches(args[2], generatorIds, new ArrayList<>());
        }

        // Tab für: /coregen give <Spieler> <GeneratorID> <Level>
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return StringUtil.copyPartialMatches(args[3], List.of("0", "1", "2", "3", "4", "5"), new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
