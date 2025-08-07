package com.coregenerators.generatorconfigs;

import com.coregenerators.main.CoreGenerators;
import com.coregenerators.main.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class GeneratorTickTask extends BukkitRunnable {

    private final boolean debug = CoreGenerators.getInstance().getConfig().getBoolean("debug.tick-logging", false);
    private final Random random = new Random();

    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000L;

        for (PlacedGenerator generator : CoreGenerators.getInstance().getStorage().getAllGenerators()) {

            if (debug) {
                Bukkit.getLogger().info("[DEBUG] Prüfe Generator bei " + generator.getLocation());
            }

            long fuelLeft;

            if (generator.isPaused()) {
                // Beim pausierten Generator die eingefrorene Zeit nutzen
                fuelLeft = generator.getPausedFuelSeconds();
                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist pausiert, verbleibende Zeit: " + fuelLeft);
                }
            } else if (generator.isActive()) {
                fuelLeft = generator.getFuelEndTime() - now;
                if (fuelLeft <= 0) {
                    generator.setActive(false);
                    generator.setFuelEndTime(now); // Verhindert negatives Veralten
                    if (debug) {
                        Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist leer und wird deaktiviert.");
                    }
                    continue;
                }
            } else {
                // Generator ist nicht aktiv und nicht pausiert → keine Verarbeitung
                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist ausgeschaltet.");
                }
                continue;
            }

            // Generator-Daten holen
            Generator genData = CoreGenerators.generators.get(generator.getGeneratorId().toLowerCase());
            if (genData == null) {
                Bukkit.getLogger().warning("[CoreGenerators] Ungültige Generator-ID: '" + generator.getGeneratorId() + "' bei " + generator.getLocation());
                continue;
            }

            UpgradeLevel upgradeLevel = UpgradeLevel.get(generator.getUpgradeLevel());
            if (upgradeLevel == null) {
                Bukkit.getLogger().warning("[CoreGenerators] Kein gültiges UpgradeLevel für Generator bei " + generator.getLocation());
                continue;
            }

            int adjustedInterval = (int) Math.max(1, genData.getInterval() * upgradeLevel.getIntervalMultiplier());
            generator.setTickCount(generator.getTickCount() + 1);

            if (debug) {
                Bukkit.getLogger().info("[DEBUG] TickCount: " + generator.getTickCount() + " / " + adjustedInterval + " für " + generator.getGeneratorId());
            }

            if (generator.getTickCount() >= adjustedInterval) {
                generator.setTickCount(0);

                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Drop wird erzeugt für Generator " + generator.getGeneratorId() + " bei " + generator.getLocation());
                }

                GeneratorDrop selectedDrop = null;
                for (GeneratorDrop drop : genData.getDrops()) {
                    if (random.nextDouble() <= drop.getChance()) {
                        selectedDrop = drop;
                        break;
                    }
                }

                if (selectedDrop == null && !genData.getDrops().isEmpty()) {
                    selectedDrop = genData.getDrops().get(0);
                    if (debug) {
                        Bukkit.getLogger().info("[DEBUG] Kein Drop per Zufall, fallback: " + selectedDrop.getMaterial());
                    }
                }

                if (selectedDrop != null) {
                    Location dropLoc = generator.getLocation().clone().add(0.5, 1, 0.5);
                    World world = dropLoc.getWorld();
                    if (world != null) {
                        int dropAmount = selectedDrop.getAmount() * upgradeLevel.getDropMultiplier();
                        ItemStack dropItem = new ItemStack(selectedDrop.getMaterial(), dropAmount);
                        world.dropItemNaturally(dropLoc, dropItem);

                        if (debug) {
                            Bukkit.getLogger().info("[DEBUG] Drop erzeugt: " + dropAmount + "x " + selectedDrop.getMaterial() + " bei " + dropLoc);
                        }
                    } else {
                        Bukkit.getLogger().warning("[CoreGenerators] Welt ist null bei Drop-Location: " + dropLoc);
                    }
                }
            }
        }
    }
}
