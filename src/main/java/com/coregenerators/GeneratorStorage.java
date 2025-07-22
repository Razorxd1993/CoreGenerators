package com.coregenerators.util;

import com.coregenerators.CoreGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GeneratorStorage {

    private static final Map<UUID, List<Location>> playerGenerators = new HashMap<>();

    // Speicherpfad: plugins/CoreGenerators/storage/database.yml
    private static final File file = new File(CoreGenerators.getInstance().getDataFolder(), "storage/database.yml");

    static {
        file.getParentFile().mkdirs(); // "storage" Ordner erstellen
    }

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

        ConfigurationSection generatorsSection = config.getConfigurationSection("Generators");
        if (generatorsSection == null) return;

        for (String uuidString : generatorsSection.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                CoreGenerators.getInstance().getLogger().warning("Ungültige UUID im Speicher gefunden: " + uuidString + " – wird übersprungen.");
                continue;
            }

            List<Location> locations = new ArrayList<>();

            ConfigurationSection section = generatorsSection.getConfigurationSection(uuidString);
            if (section == null) continue;

            for (String key : section.getKeys(false)) {
                if (key.equalsIgnoreCase("generators")) continue;

                String locStr = section.getString(key);
                if (locStr == null) continue;

                String[] parts = locStr.split(",");
                if (parts.length != 4) continue;

                String worldName = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);

                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                Location loc = new Location(world, x, y, z);
                locations.add(loc);
            }

            if (!locations.isEmpty()) {
                playerGenerators.put(uuid, locations);
            }
        }

        CoreGenerators.getInstance().getLogger().info("Generator-Daten geladen: " + playerGenerators.size() + " Spieler mit Generatoren.");
    }

    public static void save() {
        config.set("Generators", null); // Vorherige Daten löschen

        for (Map.Entry<UUID, List<Location>> entry : playerGenerators.entrySet()) {
            UUID uuid = entry.getKey();
            List<Location> locations = entry.getValue();

            if (locations == null || locations.isEmpty()) continue;

            config.set("Generators." + uuid.toString() + ".generators", locations.size());

            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
                config.set("Generators." + uuid.toString() + "." + (i + 1), locStr);
            }

            CoreGenerators.getInstance().getLogger().info("Daten gespeichert für Spieler: " + uuid.toString() + " (" + locations.size() + " Generatoren)");
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
