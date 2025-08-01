package com.coregenerators.listener;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.main.CoreGenerators;
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

    // Zwischenspeicher für NBT-Daten vor dem Platzieren
    private final Map<UUID, NBTItem> nbtCache = new ConcurrentHashMap<>();

    @EventHandler
    public void onPrePlace(FurniturePrePlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
            Bukkit.getLogger().warning("[CoreGenerators] PrePlace: Spieler " + player.getName() + " hatte kein Item in der Hand.");
            return;
        }

        try {
            NBTItem nbt = new NBTItem(player.getInventory().getItemInMainHand().clone());
            nbtCache.put(player.getUniqueId(), nbt);
            Bukkit.getLogger().info("[CoreGenerators] PrePlace: NBT-Daten für Spieler " + player.getName() + " gecached.");
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CoreGenerators] PrePlace: Fehler beim Lesen von NBT-Daten: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlaced(FurniturePlacedEvent e) {
        Player player = e.getPlayer();
        Location location = e.getBukkitEntity().getLocation().getBlock().getLocation();
        UUID owner = player.getUniqueId();

        NBTItem cachedNbt = nbtCache.remove(owner);

        if (cachedNbt == null) {
            Bukkit.getLogger().warning("[CoreGenerators] FurniturePlacedEvent: Kein NBT-Cache für Spieler " + player.getName());
            return;
        }

        String generatorId = null;
        int upgradeLevel = 0;
        long fuelEndTime = System.currentTimeMillis() / 1000L + 1800; // Default 30 Minuten

        if (cachedNbt.hasTag("coregen_id")) {
            generatorId = cachedNbt.getString("coregen_id").toLowerCase();
        } else {
            Bukkit.getLogger().warning("[CoreGenerators] NBT-Daten enthalten kein 'coregen_id' Tag für Spieler " + player.getName());
            return;
        }

        if (cachedNbt.hasTag("coregen_upgrade")) {
            upgradeLevel = cachedNbt.getInteger("coregen_upgrade");
        }

        if (cachedNbt.hasTag("coregen_fuel")) {
            fuelEndTime = cachedNbt.getLong("coregen_fuel");
        }

        Bukkit.getLogger().info("[CoreGenerators] Generator platziert: " + generatorId + " @ " + location + " (Upgrade: " + upgradeLevel + ", FuelEndTime: " + fuelEndTime + ")");

        PlacedGenerator gen = new PlacedGenerator(location, generatorId, owner, fuelEndTime, upgradeLevel);
        CoreGenerators.getInstance().getStorage().addGenerator(gen);
        CoreGenerators.getInstance().getStorage().saveGenerator(gen);
    }
}
