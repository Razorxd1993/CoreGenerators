package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlacedGenerator {

    private final UUID owner;
    private final Location location;
    private final String generatorId;
    private long fuelEndTime;
    private long lastTick;
    private int upgradeLevel;
    private int tickCount;
    private boolean active = true;
    private long pausedFuelSeconds = -1; // -1 bedeutet: nicht pausiert
    private final Set<UUID> members = new HashSet<>();

    public PlacedGenerator(Location location, String generatorId, UUID owner, long fuelEndTime, int upgradeLevel) {
        this.owner = owner;
        this.location = location;
        this.generatorId = generatorId;
        this.fuelEndTime = fuelEndTime;
        this.upgradeLevel = upgradeLevel;
        this.lastTick = System.currentTimeMillis();
    }

    // === Besitzer, Location, Generator-Definition ===
    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public Generator getGenerator() {
        Generator gen = CoreGenerators.generators.get(this.generatorId.toLowerCase());
        if (gen == null) {
            Bukkit.getLogger().warning("[CoreGenerators] WARNUNG: Kein Generator gefunden mit ID '" + this.generatorId + "'");
        }
        return gen;
    }

    // === Fuel-Zeit und Tick-Daten ===
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

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    // === Upgrade-Level ===
    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
    }

    // === Status / Aktivität ===
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPaused() {
        return !active && pausedFuelSeconds >= 0;
    }

    // === Fuel hinzufügen (z. B. beim Aufladen) ===
    public void addFuel(int seconds) {
        long now = System.currentTimeMillis() / 1000L;
        long currentEnd = this.fuelEndTime;

        if (currentEnd < now) {
            currentEnd = now;
        }

        long newEnd = currentEnd + seconds;
        long maxEnd = now + (24 * 60 * 60); // maximal 24 Stunden

        if (newEnd > maxEnd) {
            newEnd = maxEnd;
        }

        this.fuelEndTime = newEnd;

        // Reaktiviere Generator, falls deaktiviert und wieder Laufzeit vorhanden
        if (!this.active && this.fuelEndTime > now) {
            this.active = true;
            this.pausedFuelSeconds = -1;
        }
    }

    // === Pausieren / Fortsetzen ===
    public void pause() {
        if (!active) return;

        long now = System.currentTimeMillis() / 1000L;
        long remaining = fuelEndTime - now;
        if (remaining < 0) remaining = 0;

        this.pausedFuelSeconds = remaining;
        this.active = false;
    }

    public void resume() {
        if (active || pausedFuelSeconds < 0) return;

        long now = System.currentTimeMillis() / 1000L;
        this.fuelEndTime = now + pausedFuelSeconds;
        this.pausedFuelSeconds = -1;
        this.active = true;
    }

    // === Berechnung Restzeit (z. B. für GUI) ===
    public long getRemainingFuelTimeInSeconds() {
        if (isPaused()) {
            return pausedFuelSeconds;
        }
        long now = System.currentTimeMillis() / 1000L;
        return Math.max(0, fuelEndTime - now);
    }

    public long getPausedFuelSeconds() {
        return pausedFuelSeconds;
    }

    public void setPausedFuelSeconds(long pausedFuelSeconds) {
        this.pausedFuelSeconds = pausedFuelSeconds;
    }

    // === Mitgliederverwaltung ===
    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isMember(UUID uuid) {
        return owner.equals(uuid) || members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

}
