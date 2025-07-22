package com.coregenerators;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Material;

import java.util.List;

public class Generator {
    private final String id;
    private final CustomBlock block;
    private final Material fuel;
    private final int interval;
    private final List<GeneratorDrop> drops;

    public Generator(String id, Material fuel, int interval, List<GeneratorDrop> drops) {
        this.id = id;
        this.block = block;
        this.fuel = fuel;
        this.interval = interval;
        this.drops = drops;
    }

    public String getId() {
        return id;
    }

    public CustomBlock getBlock() {
        return block;
    }

    public Material getFuel() {
        return fuel;
    }

    public int getInterval() {
        return interval;
    }

    public List<GeneratorDrop> getDrops() {
        return drops;
    }
}
