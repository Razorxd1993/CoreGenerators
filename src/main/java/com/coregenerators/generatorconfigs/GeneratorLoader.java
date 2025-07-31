package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class GeneratorLoader {

    public static void loadGenerators() {
        CoreGenerators plugin = CoreGenerators.getInstance();

        File file = new File(plugin.getDataFolder(), "generators.yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Die Datei generators.yml wurde nicht gefunden.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("generators");

        if (section == null) {
            plugin.getLogger().warning("Keine Generatoren in generators.yml gefunden.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection genSection = section.getConfigurationSection(key);
            if (genSection == null) continue;

            String itemsAdderId = genSection.getString("itemsadder");
            CustomFurniture furniture = null;
            CustomStack stack = null;
            ItemStack itemStack = null;

            if (itemsAdderId != null && !itemsAdderId.isEmpty()) {
                try {
                    stack = CustomStack.getInstance(itemsAdderId);
                    if (stack == null) {
                        plugin.getLogger().warning("ItemsAdder-ID '" + itemsAdderId + "' konnte nicht gefunden werden.");
                    } else {
                        if (stack instanceof CustomFurniture f) {
                            furniture = f;
                        }
                        itemStack = stack.getItemStack().clone();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Laden von ItemsAdder-Objekt '" + itemsAdderId + "': " + e.getMessage());
                }
            }

            // Material-Fallback
            Material fallbackMaterial = Material.CHEST;
            try {
                fallbackMaterial = Material.valueOf(genSection.getString("material", "CHEST").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ungültiges Material für Generator '" + key + "': " + e.getMessage());
            }

            int interval = genSection.getInt("interval", 500);
            int customData = genSection.getInt("customdata", 0);

            // Drops laden
            ConfigurationSection dropSection = genSection.getConfigurationSection("drop");
            List<GeneratorDrop> drops = GeneratorDrop.fromSection(dropSection);

            // Debug-Ausgabe
            if (plugin.getConfig().getBoolean("debug.generator-loading", false)) {
                plugin.getLogger().info("Generator '" + key + "' lädt " + drops.size() + " Drop(s):");
                for (GeneratorDrop drop : drops) {
                    plugin.getLogger().info("  -> " + drop.getMaterial() + " x" + drop.getAmount() + " @ " + drop.getChance());
                }
            }

            Generator generator = new Generator(key, furniture, fallbackMaterial, interval, customData, drops, itemsAdderId);

            if (itemStack == null) {
                itemStack = new ItemStack(fallbackMaterial);
            }

            generator.setItem(itemStack);
            CoreGenerators.generators.put(key, generator);
            plugin.getLogger().info("Generator geladen: " + key);
        }
    }
}
