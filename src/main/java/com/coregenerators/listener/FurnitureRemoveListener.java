package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FurnitureRemoveListener implements Listener {

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        if (event.getFurniture() == null) return;

        Location loc = event.getFurniture().getArmorstand().getLocation().getBlock().getLocation();

        PlacedGenerator placed = CoreGenerators.getInstance().getStorage().getPlacedGenerator(loc);
        if (placed == null) return;

        CoreGenerators.getInstance().getStorage().removeGenerator(placed);
        CoreGenerators.getInstance().getStorage().saveAll();
    }
}
