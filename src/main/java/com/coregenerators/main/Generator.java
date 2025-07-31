package com.coregenerators.main;

import com.coregenerators.generatorconfigs.GeneratorDrop;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Generator {

    private final String id;
    private final CustomFurniture customFurniture;
    private final Material fallbackMaterial;
    private final int interval;
    private final int customData;
    private final List<GeneratorDrop> drops;
    private final int maxFuel;
    private final String itemsAdderId;

    private ItemStack item;

    public Generator(String id, CustomFurniture furniture, Material fallbackMaterial, int interval, int customData, List<GeneratorDrop> drops, String itemsAdderId) {
        this.id = id;
        this.customFurniture = furniture;
        this.fallbackMaterial = fallbackMaterial;
        this.interval = interval;
        this.customData = customData;
        this.drops = drops;
        this.maxFuel = 100;
        this.itemsAdderId = itemsAdderId;
    }

    public String getItemsAdderId() {
        return itemsAdderId;
    }

    public String getId() {
        return id;
    }

    public CustomFurniture getFurniture() {
        return customFurniture;
    }

    public Material getFallbackMaterial() {
        return fallbackMaterial;
    }

    public int getInterval() {
        return interval;
    }

    public int getCustomData() {
        return customData;
    }

    public List<GeneratorDrop> getDrops() {
        return drops;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        if (item != null) return item.clone();
        if (customFurniture != null) return customFurniture.getItemStack().clone();
        return new ItemStack(fallbackMaterial);
    }

}
