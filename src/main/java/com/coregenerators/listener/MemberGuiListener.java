package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.MemberGui;
import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemberGuiListener implements Listener {

    private final CoreGenerators plugin;
    private final ChatInputListener chatInputListener;

    // Map von SpielerUUID zu der aktuell geöffneten Generator-Location
    private final Map<UUID, Location> openGeneratorLocations = new HashMap<>();

    public MemberGuiListener(CoreGenerators plugin) {
        this.plugin = plugin;
        this.chatInputListener = plugin.getChatInputListener();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!"§8Mitglieder".equals(title)) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        Location generatorLocation = openGeneratorLocations.get(player.getUniqueId());
        if (generatorLocation == null) {
            player.closeInventory();
            player.sendMessage("§cKein Generator gefunden.");
            return;
        }

        PlacedGenerator placed = plugin.getStorage().getPlacedGenerator(generatorLocation);
        if (placed == null) {
            player.closeInventory();
            player.sendMessage("§cKein Generator gefunden.");
            return;
        }

        // Hinzufügen-Button (Slot 26)
        if (slot == 26) {
            player.closeInventory();

            if (chatInputListener == null) {
                player.sendMessage("§cChatInputListener nicht gefunden.");
                return;
            }

            chatInputListener.waitForInput(player, input -> {
                if (input == null || input.isBlank()) {
                    player.sendMessage("§cKein Name eingegeben.");
                    return false;
                }

                Player target = Bukkit.getPlayerExact(input);
                if (target == null) {
                    player.sendMessage("§cSpieler nicht gefunden oder offline.");
                    return false;
                }

                UUID targetUUID = target.getUniqueId();

                if (placed.getMembers().contains(targetUUID)) {
                    player.sendMessage("§cDieser Spieler ist bereits Mitglied.");
                    return false;
                }

                if (placed.getOwner().equals(targetUUID)) {
                    player.sendMessage("§cDer Besitzer ist automatisch Mitglied.");
                    return false;
                }

                placed.getMembers().add(targetUUID);
                plugin.getStorage().saveGenerator(placed);

                player.sendMessage("§aSpieler §e" + target.getName() + " §aals Mitglied hinzugefügt.");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        MemberGui.open(player, placed);
                        // Update Map, falls nötig
                        openGeneratorLocations.put(player.getUniqueId(), placed.getLocation());
                    }
                }.runTask(plugin);

                return true;
            });

            return;
        }

        // Klick auf Mitgliedskopf = Mitglied entfernen (Slots 0-25)
        if (slot >= 0 && slot < 26) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            UUID memberUUID = meta.getOwningPlayer().getUniqueId();

            if (!placed.getMembers().contains(memberUUID)) return;

            placed.getMembers().remove(memberUUID);
            plugin.getStorage().saveGenerator(placed);

            player.sendMessage("§cSpieler §e" + meta.getOwningPlayer().getName() + " §cwurde als Mitglied entfernt.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    MemberGui.open(player, placed);
                    openGeneratorLocations.put(player.getUniqueId(), placed.getLocation());
                }
            }.runTask(plugin);
        }
    }

    /**
     * Wird aufgerufen, wenn das Mitglieder-GUI geöffnet wird,
     * um die Location des aktuellen Generators zu speichern.
     */
    public void setOpenGenerator(Player player, PlacedGenerator placed) {
        openGeneratorLocations.put(player.getUniqueId(), placed.getLocation());
    }

    /**
     * Wird aufgerufen, wenn das Mitglieder-GUI geschlossen wird,
     * um den Cache zu leeren.
     */
    public void removeOpenGenerator(Player player) {
        openGeneratorLocations.remove(player.getUniqueId());
    }
}
