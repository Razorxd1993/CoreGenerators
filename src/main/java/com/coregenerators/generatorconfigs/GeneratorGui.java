package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeneratorGui {

    public static void open(Player player, PlacedGenerator placed) {
        File file = new File(CoreGenerators.getInstance().getDataFolder(), "gui.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection guiSection = config.getConfigurationSection("generator_gui");
        if (guiSection == null) {
            player.sendMessage("§cGUI-Konfiguration nicht gefunden.");
            return;
        }

        String title = guiSection.getString("title", "§8Generator");
        int size = guiSection.getInt("size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSection = guiSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;

                int slot = itemSection.getInt("slot", -1);
                if (slot < 0 || slot >= size) continue;

                String materialName = itemSection.getString("material", "STONE");
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) material = Material.STONE;

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                // Slot 22 = An/Aus-Button: Überschreibe Name & Lore dynamisch
                if (slot == 22) {
                    if (placed.isActive()) {
                        meta.setDisplayName("§aGenerator an");
                        List<String> lore = new ArrayList<>();
                        lore.add("§7Klicke, um den Generator auszuschalten.");
                        meta.setLore(lore);
                    } else {
                        meta.setDisplayName("§cGenerator aus");
                        List<String> lore = new ArrayList<>();
                        lore.add("§7Klicke, um den Generator einzuschalten.");
                        meta.setLore(lore);
                    }
                } else {
                    // Name aus config
                    meta.setDisplayName(itemSection.getString("name", ""));

                    // Lore mit Platzhalter ersetzen
                    List<String> lore = new ArrayList<>();
                    for (String line : itemSection.getStringList("lore")) {
                        int currentLevel = placed.getUpgradeLevel();
                        int nextLevel = Math.min(currentLevel + 1, 6);
                        double nextUpgradeCost = nextLevel * 5000;

                        line = line.replace("%time%", getRemainingTime(placed));
                        line = line.replace("%upgrade%", String.valueOf(currentLevel));
                        line = line.replace("%upgrade_cost%", nextLevel > currentLevel ? String.valueOf((int) nextUpgradeCost) : "§cMax");
                        lore.add(line);
                    }
                    meta.setLore(lore);
                }

                item.setItemMeta(meta);
                gui.setItem(slot, item);
            }
        }

        player.openInventory(gui);
    }

    private static String getRemainingTime(PlacedGenerator placed) {
        long now = System.currentTimeMillis() / 1000;
        long remaining = placed.getFuelEndTime() - now;
        if (remaining <= 0) return "§cInaktiv";

        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}
