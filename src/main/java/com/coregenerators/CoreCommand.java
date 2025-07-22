package com.coregenerators.commands;

import com.coregenerators.CoreGenerators;
import com.coregenerators.Generator;
import com.coregenerators.util.GeneratorStorage;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cVerwendung: /coregen <give|reload|check|remove>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage("§c/coregen give <generator> [player]");
                    return true;
                }

                String generatorId = args[1];
                Player target = (args.length >= 3) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);

                if (target == null) {
                    sender.sendMessage("§cSpieler nicht gefunden.");
                    return true;
                }

                Generator generator = CoreGenerators.generators.get(generatorId);
                if (generator == null) {
                    sender.sendMessage("§cGenerator '" + generatorId + "' nicht gefunden.");
                    return true;
                }

                ItemStack item;
                if (generator.getCustomBlock() != null) {
                    item = generator.getCustomBlock().getItemStack();
                } else {
                    item = new ItemStack(generator.getFallbackMaterial());
                }

                target.getInventory().addItem(item);
                sender.sendMessage("§aGenerator '" + generatorId + "' wurde an " + target.getName() + " gegeben.");
            }

            case "reload" -> {
                CoreGenerators.getInstance().reloadConfig();
                sender.sendMessage("§aKonfiguration wurde neu geladen.");
            }

            case "check" -> {
                // Optional: Implementiere Tickstatus-Anzeige
                sender.sendMessage("§7(Tickstatus wird hier angezeigt...)");
            }

            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage("§c/coregen remove <spieler> <anzahl>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                int amount = Integer.parseInt(args[2]);

                if (target == null) {
                    sender.sendMessage("§cSpieler nicht gefunden.");
                    return true;
                }

                List<Location> playerGenerators = GeneratorStorage.getGenerators(target.getUniqueId());
                for (int i = 0; i < Math.min(amount, playerGenerators.size()); i++) {
                    Location loc = playerGenerators.remove(0);
                    loc.getBlock().setType(Material.AIR);
                }

                sender.sendMessage("§a" + amount + " Generator(en) von " + target.getName() + " entfernt.");
            }

            default -> sender.sendMessage("§cUnbekannter Subcommand.");
        }

        return true;
    }
}