package com.coregenerators.util;

import com.coregenerators.CoreGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GeneratorStorage {

    private static final Map<UUID, List<Location>> playerGenerators = new HashMap<>();
    private static final File file = new File(CoreGenerators.getInstance().getDataFolder(), "storage.yml");
    private static FileConfiguration config;

    public static void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        playerGenerators.clear();

        if (config.getConfigurationSection("generators") == null) return;

        for (String uuidStr : config.getConfigurationSection("generators").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<Location> locations = new ArrayList<>();

            for (String locStr : config.getStringList("generators." + uuidStr)) {
                String[] parts = locStr.split(",");
                String worldName = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);

                Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                locations.add(loc);
            }

            playerGenerators.put(uuid, locations);
        }
    }

    public static void save() {
        config.set("generators", null); // clear
        for (UUID uuid : playerGenerators.keySet()) {
            List<String> locStrings = new ArrayList<>();
            for (Location loc : playerGenerators.get(uuid)) {
                String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
                locStrings.add(locStr);
            }
            config.set("generators." + uuid.toString(), locStrings);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addGenerator(UUID owner, Location location) {
        playerGenerators.computeIfAbsent(owner, k -> new ArrayList<>()).add(location);
        save();
    }

    public static void removeGenerator(UUID owner, Location location) {
        List<Location> locs = playerGenerators.get(owner);
        if (locs == null) return;
        locs.removeIf(loc -> loc.equals(location));
        if (locs.isEmpty()) {
            playerGenerators.remove(owner);
        }
        save();
    }

    public static List<Location> getGenerators(UUID owner) {
        return playerGenerators.getOrDefault(owner, new ArrayList<>());
    }
}
