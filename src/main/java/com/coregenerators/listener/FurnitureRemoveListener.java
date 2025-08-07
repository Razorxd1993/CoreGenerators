package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class FurnitureRemoveListener implements Listener {

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        if (event.getFurniture() == null || event.getPlayer() == null) return;

        Player player = event.getPlayer();
        Location loc = event.getFurniture().getArmorstand().getLocation().getBlock().getLocation();

        PlacedGenerator placed = CoreGenerators.getInstance().getStorage().getPlacedGenerator(loc);
        if (placed == null) return;

        // 🔐 Spieler ist nicht der Besitzer?
        if (!player.getUniqueId().equals(placed.getOwner())) {
            player.sendMessage("§cDu bist nicht der Besitzer dieses Generators.");
            event.setCancelled(true);
            return;
        }

        // Spieler ist der Besitzer – Generator-Item mit NBT erzeugen
        ItemStack generatorItem = CoreGenerators.getInstance().getStorage().createGeneratorItem(
                placed.getGeneratorId(),
                placed.getUpgradeLevel(),
                placed.getFuelEndTime()
        );

        // Item geben oder droppen
        if (generatorItem != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(generatorItem);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), generatorItem);
            }
            player.sendMessage("§9§lCORELITH §f§l>> §aDu hast deinen Generator erfolgreich abgebaut!");
        }

        // Speicher aktualisieren
        CoreGenerators.getInstance().getStorage().removeGenerator(placed);
        CoreGenerators.getInstance().getStorage().saveAll();
    }
}
