package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.Events.FurniturePlacedEvent;
import dev.lone.itemsadder.api.Events.FurniturePrePlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FurniturePlaceListener implements Listener {

    // Zwischenspeicher für die NBT-Daten vor dem Platzieren
    private final Map<UUID, NBTItem> nbtCache = new ConcurrentHashMap<>();

    @EventHandler
    public void onPrePlace(FurniturePrePlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
            Bukkit.getLogger().warning("[CoreGenerators] PrePlace: Spieler " + player.getName() + " hatte kein Item in der Hand.");
            return;
        }

        try {
            NBTItem nbt = new NBTItem(player.getInventory().getItemInMainHand().clone());
            nbtCache.put(uuid, nbt);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CoreGenerators] PrePlace: Fehler beim Lesen von NBT-Daten: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlaced(FurniturePlacedEvent e) {
        Player player = e.getPlayer();
        String itemsAdderId = e.getNamespacedID(); // z. B. coregenerators:coregenerator
        Location location = e.getBukkitEntity().getLocation().getBlock().getLocation();
        UUID owner = player.getUniqueId();

        // Generator anhand ItemsAdder-ID finden
        String matchedKey = null;
        Generator matchedGen = null;
        for (Map.Entry<String, Generator> entry : CoreGenerators.generators.entrySet()) {
            Generator g = entry.getValue();
            if (g.getItemsAdderId() != null && g.getItemsAdderId().equals(itemsAdderId)) {
                matchedKey = entry.getKey();
                matchedGen = g;
                break;
            }
        }

        if (matchedKey == null) {
            Bukkit.getLogger().warning("[CoreGenerators] Keine passende Generator-Definition für " + itemsAdderId);
            return;
        }

        // NBT-Daten aus Cache lesen
        int upgradeLevel = 0;
        long fuelEndTime = System.currentTimeMillis() / 1000L + 1800; // Default: 30 Minuten

        NBTItem cachedNbt = nbtCache.remove(owner);
        if (cachedNbt != null) {
            if (cachedNbt.hasTag("coregen_upgrade")) {
                upgradeLevel = cachedNbt.getInteger("coregen_upgrade");
            }
            if (cachedNbt.hasTag("coregen_fuel")) {
                fuelEndTime = cachedNbt.getLong("coregen_fuel");
            }
        }

        // Generator registrieren
        PlacedGenerator gen = new PlacedGenerator(location, matchedKey, owner, fuelEndTime, upgradeLevel);
        CoreGenerators.getInstance().getStorage().addGenerator(gen);
        CoreGenerators.getInstance().getStorage().saveGenerator(gen);

        Bukkit.getLogger().info("[CoreGenerators] Furniture-Platzierung erkannt und gespeichert: " + matchedKey + " @ " + location);
    }
}
