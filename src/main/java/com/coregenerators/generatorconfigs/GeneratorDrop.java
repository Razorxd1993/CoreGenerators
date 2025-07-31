package com.coregenerators.generatorconfigs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class GeneratorDrop {

    private final Material material;
    private final int amount;
    private final double chance;

    public GeneratorDrop(Material material, int amount, double chance) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }

    /**
     * Lädt alle Drops aus einer YAML-Konfiguration
     */
    public static List<GeneratorDrop> fromSection(ConfigurationSection section) {
        List<GeneratorDrop> drops = new ArrayList<>();
        if (section == null) return drops;

        for (String key : section.getKeys(false)) {
            ConfigurationSection dropSec = section.getConfigurationSection(key);
            if (dropSec == null) continue;

            try {
                Material material = Material.valueOf(dropSec.getString("material", "STONE"));
                int amount = dropSec.getInt("amount", 1);
                double chance = dropSec.getDouble("chance", 1.0);

                drops.add(new GeneratorDrop(material, amount, chance));
            } catch (IllegalArgumentException e) {
                System.out.println("[CoreGenerators] Ungültiges Drop-Material in generators.yml: " + key);
            }
        }

        return drops;
    }
}
