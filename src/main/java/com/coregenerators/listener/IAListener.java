package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.GeneratorLoader;
import com.coregenerators.main.CoreGenerators;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IAListener implements Listener {
    @EventHandler
    public void onIAReady(ItemsAdderLoadDataEvent e) {
        GeneratorLoader.loadGenerators();
        CoreGenerators.getInstance().getLogger().info("ItemsAdder-Daten geladen, Generatoren initialisiert.");
    }
}