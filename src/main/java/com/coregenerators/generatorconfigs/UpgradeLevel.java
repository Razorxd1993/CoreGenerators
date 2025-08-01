package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UpgradeLevel {

    private static final Map<Integer, UpgradeLevel> levelMap = new HashMap<>();

    private final int level;
    private final double intervalMultiplier;
    private final int dropMultiplier;
    private final double price;

    public UpgradeLevel(int level, double intervalMultiplier, int dropMultiplier, double price) {
        this.level = level;
        this.intervalMultiplier = intervalMultiplier;
        this.dropMultiplier = dropMultiplier;
        this.price = price;
    }

    public int getLevel() {
        return level;
    }

    public double getIntervalMultiplier() {
        return intervalMultiplier;
    }

    public int getDropMultiplier() {
        return dropMultiplier;
    }

    public double getPrice() {
        return price;
    }

    public static UpgradeLevel get(int level) {
        return levelMap.getOrDefault(level, levelMap.get(1));
    }

    public static boolean exists(int level) {
        return levelMap.containsKey(level);
    }

    public static void loadAll() {
        File file = new File(CoreGenerators.getInstance().getDataFolder(), "upgrades.yml");
        if (!file.exists()) {
            CoreGenerators.getInstance().saveResource("upgrades.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("upgrade_levels");
        if (section == null) return;

        levelMap.clear();

        for (String key : section.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                ConfigurationSection lvlSec = section.getConfigurationSection(key);
                if (lvlSec == null) continue;

                double intervalMult = lvlSec.getDouble("interval_multiplier", 1.0);
                int dropMult = lvlSec.getInt("drop_multiplier", 1);
                double price = lvlSec.getDouble("price", 0.0);

                levelMap.put(level, new UpgradeLevel(level, intervalMult, dropMult, price));
            } catch (Exception e) {
                CoreGenerators.getInstance().getLogger().warning("[CoreGenerators] Fehler beim Laden von Upgrade-Level: " + key);
            }
        }

        CoreGenerators.getInstance().getLogger().info("[CoreGenerators] Upgrade-Levels geladen: " + levelMap.size());
    }
}
