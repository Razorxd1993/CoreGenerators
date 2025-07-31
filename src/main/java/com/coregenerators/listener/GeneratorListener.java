package com.coregenerators.listener;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.util.Messages;
import com.coregenerators.util.Permissions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class GeneratorListener implements Listener {

    private final CoreGenerators plugin;

    public GeneratorListener(CoreGenerators plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.hasBlock()) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        PlacedGenerator generator = plugin.getStorage().getPlacedGenerator(block.getLocation());
        if (generator == null) return;

        Player player = event.getPlayer();
        if (!player.hasPermission(Permissions.USE)) {
            player.sendMessage(Messages.get("no-permission"));
            event.setCancelled(true);
            return;
        }

        player.sendMessage("§aGenerator geöffnet: " + generator.getGeneratorId());
    }
}