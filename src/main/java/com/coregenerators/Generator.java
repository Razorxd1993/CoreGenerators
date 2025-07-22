package com.coregenerators;

import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Material;

import java.util.List;

public class Generator {

    private final String id;
    private final CustomBlock customBlock;
    private final Material fallbackMaterial;
    private final int interval;
    private final int customData;
    private final List<GeneratorDrop> drops;

    public Generator(String id, CustomBlock customBlock, Material fallbackMaterial, int interval, int customData, List<GeneratorDrop> drops) {
        this.id = id;
        this.customBlock = customBlock;
        this.fallbackMaterial = fallbackMaterial;
        this.interval = interval;
        this.customData = customData;
        this.drops = drops;
    }

    public String getId() {
        return id;
    }

    public CustomBlock getCustomBlock() {
        return customBlock;
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
}
