package com.coregenerators;

import com.coregenerators.util.GeneratorStorage;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GeneratorListener implements Listener {

    @EventHandler
    public void onItemsAdderPlace(CustomBlockPlaceEvent event) {
        String placedBlockID = event.getNamespacedID(); // Hier bekommst du die NamespacedID des platzierten Blocks
        Player player = event.getPlayer();

        // Prüfe, ob dieser Block ein Generator ist
        for (Map.Entry<String, Generator> entry : CoreGenerators.generators.entrySet()) {
            Generator gen = entry.getValue();

            if (gen.getCustomBlock() != null && gen.getCustomBlock().getNamespacedID().equals(placedBlockID)) {
                GeneratorStorage.addGenerator(player.getUniqueId(), event.getBlock().getLocation());
                player.sendMessage("§aGenerator §e" + gen.getId() + " §awurde platziert.");
                return;
            }
        }
    }

    @EventHandler
    public void onVanillaPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        for (Map.Entry<String, Generator> entry : CoreGenerators.generators.entrySet()) {
            Generator gen = entry.getValue();

            // Wenn der Generator kein CustomBlock ist und das Item-Material übereinstimmt
            if (gen.getCustomBlock() == null && gen.getFallbackMaterial() == item.getType()) {
                GeneratorStorage.addGenerator(player.getUniqueId(), block.getLocation());
                player.sendMessage("§aGenerator §e" + gen.getId() + " §awurde platziert.");
                return;
            }
        }
    }
}