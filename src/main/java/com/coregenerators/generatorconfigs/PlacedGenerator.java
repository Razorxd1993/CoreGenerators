package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import org.bukkit.Location;

import java.util.UUID;

public class PlacedGenerator {

    private final UUID owner;
    private final Location location;
    private final String generatorId;
    private long fuelEndTime;
    private long lastTick;
    private int upgradeLevel;
    private int tickCount;

    public PlacedGenerator(Location location, String generatorId, UUID owner, long fuelEndTime, int upgradeLevel) {
        this.owner = owner;
        this.location = location;
        this.generatorId = generatorId;
        this.fuelEndTime = fuelEndTime;
        this.upgradeLevel = upgradeLevel;
        this.lastTick = System.currentTimeMillis();
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public long getFuelEndTime() {
        return fuelEndTime;
    }

    public void setFuelEndTime(long fuelEndTime) {
        this.fuelEndTime = fuelEndTime;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    /**
     * Fügt dem Generator eine bestimmte Menge an Laufzeit (in Sekunden) hinzu,
     * aber maximal bis 24 Stunden Gesamtzeit.
     */
    public void addFuel(int seconds) {
        long now = System.currentTimeMillis() / 1000;
        long currentEnd = this.fuelEndTime;

        if (currentEnd < now) {
            currentEnd = now;
        }

        long newEnd = currentEnd + seconds;
        long maxEnd = now + (24 * 60 * 60); // 24h

        if (newEnd > maxEnd) {
            newEnd = maxEnd;
        }

        this.fuelEndTime = newEnd;
    }

    public Generator getGenerator() {
        return CoreGenerators.generators.get(this.generatorId);
    }
}
