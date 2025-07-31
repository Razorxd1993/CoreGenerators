package com.coregenerators.util;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import com.coregenerators.generatorconfigs.PlacedGenerator;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GeneratorStorage {

    private final Map<UUID, List<PlacedGenerator>> playerGeneratorMap = new HashMap<>();
    private final Map<Location, PlacedGenerator> placedGeneratorMap = new HashMap<>();

    public void addGenerator(PlacedGenerator generator) {
        placedGeneratorMap.put(generator.getLocation(), generator);
        playerGeneratorMap.computeIfAbsent(generator.getOwner(), k -> new ArrayList<>()).add(generator);
    }

    public void removeGenerator(PlacedGenerator generator) {
        placedGeneratorMap.remove(generator.getLocation());
        List<PlacedGenerator> list = playerGeneratorMap.get(generator.getOwner());
        if (list != null) {
            list.remove(generator);
            if (list.isEmpty()) {
                playerGeneratorMap.remove(generator.getOwner());
                File file = new File(CoreGenerators.getInstance().getDataFolder(), "storage/" + generator.getOwner() + ".yml");
                if (file.exists()) file.delete();
            } else {
                saveAll();
            }
        }
    }

    public void loadAllGenerators() {
        File folder = new File(CoreGenerators.getInstance().getDataFolder(), "storage");
        if (!folder.exists()) return;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                UUID owner = UUID.fromString(file.getName().replace(".yml", ""));
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                List<PlacedGenerator> list = new ArrayList<>();
                for (String key : config.getConfigurationSection("generators").getKeys(false)) {
                    String path = "generators." + key;

                    String world = config.getString(path + ".world");
                    int x = config.getInt(path + ".x");
                    int y = config.getInt(path + ".y");
                    int z = config.getInt(path + ".z");
                    String id = config.getString(path + ".id");
                    long fuel = config.getLong(path + ".fuelEndTime");
                    int upgrade = config.getInt(path + ".upgrade");

                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    PlacedGenerator gen = new PlacedGenerator(loc, id, owner, fuel, upgrade);

                    list.add(gen);
                    placedGeneratorMap.put(loc, gen);
                }

                playerGeneratorMap.put(owner, list);
            } catch (Exception e) {
                CoreGenerators.getInstance().getLogger().warning("Fehler beim Laden: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public void saveGenerator(PlacedGenerator generator) {
        UUID uuid = generator.getOwner();
        File folder = new File(CoreGenerators.getInstance().getDataFolder(), "storage");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String foundKey = null;
        if (config.contains("generators")) {
            for (String key : config.getConfigurationSection("generators").getKeys(false)) {
                String path = "generators." + key;

                int x = config.getInt(path + ".x");
                int y = config.getInt(path + ".y");
                int z = config.getInt(path + ".z");
                String world = config.getString(path + ".world");

                if (x == generator.getLocation().getBlockX()
                        && y == generator.getLocation().getBlockY()
                        && z == generator.getLocation().getBlockZ()
                        && world.equals(generator.getLocation().getWorld().getName())) {
                    foundKey = key;
                    break;
                }
            }
        }

        // Wenn gefunden, existierenden Eintrag überschreiben, sonst neuen Index nutzen
        String path = "generators." + (foundKey != null ? foundKey : getNextFreeIndex(config));
        config.set(path + ".world", generator.getLocation().getWorld().getName());
        config.set(path + ".x", generator.getLocation().getBlockX());
        config.set(path + ".y", generator.getLocation().getBlockY());
        config.set(path + ".z", generator.getLocation().getBlockZ());
        config.set(path + ".fuelEndTime", generator.getFuelEndTime());
        config.set(path + ".upgrade", generator.getUpgradeLevel());
        config.set(path + ".id", generator.getGeneratorId());

        try {
            config.save(file);
        } catch (IOException e) {
            CoreGenerators.getInstance().getLogger().warning("Fehler beim Speichern von Generator für " + uuid);
            e.printStackTrace();
        }
    }

    private String getNextFreeIndex(YamlConfiguration config) {
        if (!config.contains("generators")) return "0";
        Set<String> keys = config.getConfigurationSection("generators").getKeys(false);
        int max = -1;
        for (String key : keys) {
            try {
                int k = Integer.parseInt(key);
                if (k > max) max = k;
            } catch (NumberFormatException ignored) {}
        }
        return String.valueOf(max + 1);
    }

    public void saveAll() {
        File folder = new File(CoreGenerators.getInstance().getDataFolder(), "storage");
        if (!folder.exists()) folder.mkdirs();

        for (Map.Entry<UUID, List<PlacedGenerator>> entry : playerGeneratorMap.entrySet()) {
            UUID uuid = entry.getKey();
            List<PlacedGenerator> generators = entry.getValue();

            File file = new File(folder, uuid.toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            int counter = 0;
            for (PlacedGenerator gen : generators) {
                String path = "generators." + counter++;

                config.set(path + ".world", gen.getLocation().getWorld().getName());
                config.set(path + ".x", gen.getLocation().getBlockX());
                config.set(path + ".y", gen.getLocation().getBlockY());
                config.set(path + ".z", gen.getLocation().getBlockZ());
                config.set(path + ".fuelEndTime", gen.getFuelEndTime());
                config.set(path + ".upgrade", gen.getUpgradeLevel());
                config.set(path + ".id", gen.getGeneratorId());
            }

            try {
                config.save(file);
            } catch (IOException e) {
                CoreGenerators.getInstance().getLogger().warning("Fehler beim Speichern für Spieler " + uuid);
                e.printStackTrace();
            }
        }
    }

    public Map<UUID, List<PlacedGenerator>> getPlayerGeneratorMap() {
        return playerGeneratorMap;
    }

    public Map<Location, PlacedGenerator> getPlacedGeneratorMap() {
        return placedGeneratorMap;
    }

    public PlacedGenerator getPlacedGenerator(Location location) {
        return placedGeneratorMap.get(location);
    }

    public List<PlacedGenerator> getPlayerGenerators(UUID uuid) {
        return playerGeneratorMap.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Gibt ein Generator-Item mit Standard-Aufladung (30 min) zurück.
     */
    public ItemStack createGeneratorItem(String generatorId, int upgradeLevel) {
        long defaultFuelEndTime = System.currentTimeMillis() / 1000L + 30 * 60;
        return createGeneratorItem(generatorId, upgradeLevel, defaultFuelEndTime);
    }

    /**
     * Gibt ein Generator-Item mit benutzerdefiniertem Fuel-Endzeitpunkt und Upgrade zurück.
     */
    public ItemStack createGeneratorItem(String generatorId, int upgradeLevel, long fuelEndTime) {
        Generator generator = CoreGenerators.generators.get(generatorId);
        if (generator == null) {
            CoreGenerators.getInstance().getLogger().warning("Generator nicht gefunden: " + generatorId);
            return null;
        }

        ItemStack item = generator.getItem();
        if (item == null) {
            CoreGenerators.getInstance().getLogger().warning("ItemStack ist null für Generator: " + generatorId);
            return null;
        }

        item = item.clone();

        long remainingMinutes = Math.max(0, (fuelEndTime - System.currentTimeMillis() / 1000L) / 60);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aGenerator: " + generatorId);
            List<String> lore = new ArrayList<>();
            lore.add("§7Aufladung: §a" + remainingMinutes + " min");
            lore.add("§7Upgrade: §e" + upgradeLevel);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        NBTItem nbt = new NBTItem(item);
        nbt.setString("coregen_id", generatorId);
        nbt.setInteger("coregen_upgrade", upgradeLevel);
        nbt.setLong("coregen_fuel", fuelEndTime);

        return nbt.getItem();
    }

    public void removeAllGenerators(UUID playerId) {
        List<PlacedGenerator> generators = playerGeneratorMap.remove(playerId);
        if (generators != null) {
            for (PlacedGenerator gen : generators) {
                placedGeneratorMap.remove(gen.getLocation());
            }
        }

        File folder = new File(CoreGenerators.getInstance().getDataFolder(), "storage");
        File file = new File(folder, playerId.toString() + ".yml");
        if (file.exists()) file.delete();
    }

    public Collection<PlacedGenerator> getAllGenerators() {
        return placedGeneratorMap.values();
    }
}
