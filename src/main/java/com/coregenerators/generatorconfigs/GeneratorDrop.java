package com.coregenerators.generatorconfigs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static List<GeneratorDrop> fromList(List<?> rawList) {
        List<GeneratorDrop> drops = new ArrayList<>();
        if (rawList == null) return drops;

        boolean hasGuaranteedDrop = false;

        for (Object obj : rawList) {
            if (obj instanceof Map<?, ?> rawMap) {
                try {
                    // Material
                    Object matObj = rawMap.get("material");
                    if (matObj == null) continue;
                    String matName = matObj.toString();
                    Material material = Material.valueOf(matName.toUpperCase());

                    // Amount
                    Object amountObj = rawMap.get("amount");
                    int amount = (amountObj instanceof Number) ? ((Number) amountObj).intValue() : 1;

                    // Chance
                    Object chanceObj = rawMap.get("chance");
                    double chance = (chanceObj instanceof Number) ? ((Number) chanceObj).doubleValue() : 1.0;

                    if (chance >= 1.0) {
                        hasGuaranteedDrop = true;
                    }

                    drops.add(new GeneratorDrop(material, amount, chance));
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[CoreGenerators] Fehler beim Laden eines Drops: " + e.getMessage());
                }
            }
        }

        // Optionaler Fallback: z. B. STONE mit 100 % Drop, wenn nichts sicher droppt
        if (drops.size() > 0 && !hasGuaranteedDrop) {
            GeneratorDrop fallback = drops.get(0); // z. B. ersten Eintrag forcieren
            drops.add(new GeneratorDrop(fallback.getMaterial(), fallback.getAmount(), 1.0));
            Bukkit.getLogger().info("[CoreGenerators] Kein garantierter Drop gefunden – Fallback mit 100% hinzugefügt.");
        }

        return drops;
    }
}
