package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.GeneratorGui;
import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.generatorconfigs.UpgradeLevel;
import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import com.coregenerators.util.FaweCutUtil;
import com.coregenerators.util.MessageUtil;
import de.tr7zw.nbtapi.NBTItem;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GeneratorGuiListener implements Listener {

    private final Economy economy = CoreGenerators.getEconomy();
    private final LuckPerms luckPerms = CoreGenerators.getInstance().getLuckPerms();

    private static final Map<UUID, PlacedGenerator> openGenerators = new HashMap<>();
    private static final Set<UUID> permissionMarked = new HashSet<>();

    public static void setOpenGenerator(UUID uuid, PlacedGenerator generator) {
        openGenerators.put(uuid, generator);
    }

    public static void removeOpenGenerator(UUID uuid) {
        openGenerators.remove(uuid);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            // aktuell leer
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = "§8Generator";
        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot != 11 && slot != 13 && slot != 15 && slot != 22) return;

        PlacedGenerator placed = openGenerators.get(player.getUniqueId());
        if (placed == null) {
            player.closeInventory();
            player.sendMessage("§cKein Generator an dieser Position.");
            return;
        }

        switch (slot) {
            case 11 -> {
                long now = System.currentTimeMillis() / 1000;
                long remaining = placed.getFuelEndTime() - now;

                if (remaining >= 24 * 3600) {
                    MessageUtil.send(player, "fuel.max_reached");
                    return;
                }

                double cost = 2000.0;
                if (!economy.has(player, cost)) {
                    MessageUtil.send(player, "fuel.not_enough_money");
                    return;
                }

                economy.withdrawPlayer(player, cost);
                placed.setFuelEndTime(placed.getFuelEndTime() + 30 * 60);

                CoreGenerators.getInstance().getStorage().saveGenerator(placed);

                MessageUtil.send(player, "fuel.success");
                GeneratorGui.open(player, placed);
            }

            case 13 -> {
                int currentLevel = placed.getUpgradeLevel();
                int nextLevel = currentLevel + 1;

                if (!UpgradeLevel.exists(nextLevel)) {
                    MessageUtil.send(player, "upgrade.max_level_reached"); // Nachricht definieren in messages.yml
                    return;
                }

                UpgradeLevel nextUpgrade = UpgradeLevel.get(nextLevel);
                double price = nextUpgrade.getPrice();

                if (!economy.has(player, price)) {
                    MessageUtil.send(player, "upgrade.not_enough_money"); // Nachricht definieren in messages.yml
                    return;
                }

                economy.withdrawPlayer(player, price);
                placed.setUpgradeLevel(nextLevel);

                // Generator speichern
                CoreGenerators.getInstance().getStorage().saveGenerator(placed);

                MessageUtil.send(player, "upgrade.success", String.valueOf(nextLevel), String.valueOf(price));

                // GUI neu öffnen, damit die Anzeige aktualisiert wird
                GeneratorGui.open(player, placed);
            }

            case 15 -> MessageUtil.send(player, "members.not_implemented");

            case 22 -> {
                placed.setActive(!placed.isActive());
                CoreGenerators.getInstance().getStorage().saveGenerator(placed);
                player.sendMessage("§7Generator ist jetzt " + (placed.isActive() ? "§aaktiviert" : "§cdeaktiviert") + "§7.");
                GeneratorGui.open(player, placed);
            }

        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location clicked = event.getClickedBlock().getLocation();

        PlacedGenerator placed = CoreGenerators.getInstance().getStorage().getPlacedGenerator(clicked);
        if (placed == null) return;

        event.setCancelled(true);
        setOpenGenerator(player.getUniqueId(), placed);
        GeneratorGui.open(player, placed);
    }
}
