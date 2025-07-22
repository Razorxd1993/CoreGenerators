package com.coregenerators;

import org.bukkit.Material;

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
}
