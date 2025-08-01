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

            String lowercaseKey = key.toLowerCase();

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
            List<GeneratorDrop> drops = GeneratorDrop.fromList(genSection.getList("drop"));

            Generator generator = new Generator(lowercaseKey, furniture, fallbackMaterial, interval, customData, drops, itemsAdderId);

            if (itemStack == null) {
                itemStack = new ItemStack(fallbackMaterial);
            }

            generator.setItem(itemStack);
            CoreGenerators.generators.put(lowercaseKey, generator);
            plugin.getLogger().info("Generator geladen: " + lowercaseKey + " mit " + drops.size() + " Drops");
        }
    }
}
