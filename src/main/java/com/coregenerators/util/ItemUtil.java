package com.coregenerators.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import com.coregenerators.main.CoreGenerators;

public class ItemUtil {

    public static ItemStack createGeneratorItem(String generatorId, int upgradeLevel) {
        ItemStack item = new ItemStack(Material.IRON_BLOCK); // Beispiel: Generator = Eisenblock
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bGenerator: " + generatorId);
        meta.getPersistentDataContainer().set(new NamespacedKey(CoreGenerators.getInstance(), "generatorId"), PersistentDataType.STRING, generatorId);
        meta.getPersistentDataContainer().set(new NamespacedKey(CoreGenerators.getInstance(), "upgradeLevel"), PersistentDataType.INTEGER, upgradeLevel);
        item.setItemMeta(meta);
        return item;
    }

    public static String getGeneratorId(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CoreGenerators.getInstance(), "generatorId"), PersistentDataType.STRING);
    }

    public static Integer getUpgradeLevel(ItemStack item) {
        if (!item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(
                new NamespacedKey(CoreGenerators.getInstance(), "upgradeLevel"),
                PersistentDataType.INTEGER,
                0
        );
    }
}
