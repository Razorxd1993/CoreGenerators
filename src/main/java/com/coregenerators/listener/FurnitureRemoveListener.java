package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import org.bukkit.inventory.ItemStack;

public class FurnitureRemoveListener implements Listener {

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        if (event.getFurniture() == null || event.getPlayer() == null) return;

        Player player = event.getPlayer();
        Location loc = event.getFurniture().getArmorstand().getLocation().getBlock().getLocation();

        PlacedGenerator placed = CoreGenerators.getInstance().getStorage().getPlacedGenerator(loc);
        if (placed == null) return;

        ItemStack generatorItem = CoreGenerators.getInstance().getStorage().createGeneratorItem(
                placed.getGeneratorId(),
                placed.getUpgradeLevel(),
                placed.getFuelEndTime()
        );

        if (generatorItem == null) {
            CoreGenerators.getInstance().getLogger().warning("GeneratorItem konnte nicht erstellt werden für Generator " + placed.getGeneratorId());
            return;
        }

        // Füge Item hinzu oder droppe es
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(generatorItem);
            player.sendMessage("§aDu hast deinen Generator erfolgreich abgebaut und ins Inventar bekommen!");
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), generatorItem);
            player.sendMessage("§aDu hast deinen Generator erfolgreich abgebaut!");
        }

        CoreGenerators.getInstance().getStorage().removeGenerator(placed);
        CoreGenerators.getInstance().getStorage().saveAll();
    }
}
