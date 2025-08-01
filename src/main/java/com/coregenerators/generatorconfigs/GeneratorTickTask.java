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

            // Generator inaktiv?
            if (!generator.isActive()) {
                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist ausgeschaltet.");
                }
                continue;
            }

            // Kein Fuel?
            if (generator.getFuelEndTime() <= now) {
                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist leer. Überspringe.");
                }
                continue;
            }

            Generator genData = CoreGenerators.generators.get(generator.getGeneratorId().toLowerCase());
            if (genData == null) {
                Bukkit.getLogger().warning("[CoreGenerators] Ungültige Generator-ID: '" + generator.getGeneratorId() + "' bei " + generator.getLocation());
                continue;
            }

            // UpgradeLevel holen
            UpgradeLevel upgradeLevel = UpgradeLevel.get(generator.getUpgradeLevel());
            if (upgradeLevel == null) {
                Bukkit.getLogger().warning("[CoreGenerators] Kein gültiges UpgradeLevel für Generator bei " + generator.getLocation());
                continue;
            }

            // Intervall anpassen (Minimum 1)
            int adjustedInterval = (int) Math.max(1, genData.getInterval() * upgradeLevel.getIntervalMultiplier());

            generator.setTickCount(generator.getTickCount() + 1);

            if (debug) {
                Bukkit.getLogger().info("[DEBUG] TickCount: " + generator.getTickCount() +
                        " / " + adjustedInterval + " für " + generator.getGeneratorId());
            }

            // Prüfen ob Dropzeit erreicht ist
            if (generator.getTickCount() >= adjustedInterval) {
                generator.setTickCount(0);

                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator " + generator.getGeneratorId() +
                            " droppt nun Items bei " + generator.getLocation());
                }

                // Drop anhand Chance auswählen
                GeneratorDrop selectedDrop = null;
                for (GeneratorDrop drop : genData.getDrops()) {
                    double roll = random.nextDouble();
                    if (roll <= drop.getChance()) {
                        selectedDrop = drop;
                        break;
                    }
                }

                // Fallback auf ersten Drop, falls keiner gewählt wurde
                if (selectedDrop == null && !genData.getDrops().isEmpty()) {
                    selectedDrop = genData.getDrops().get(0);
                    if (debug) {
                        Bukkit.getLogger().info("[DEBUG] Kein Drop ausgewählt, fallback zu: " + selectedDrop.getMaterial());
                    }
                }

                if (selectedDrop != null) {
                    Location loc = generator.getLocation().clone().add(0.5, 1, 0.5);
                    World world = loc.getWorld();
                    if (world != null) {
                        int dropAmount = selectedDrop.getAmount() * upgradeLevel.getDropMultiplier();
                        ItemStack item = new ItemStack(selectedDrop.getMaterial(), dropAmount);
                        world.dropItemNaturally(loc, item);

                        if (debug) {
                            Bukkit.getLogger().info("[DEBUG] Drop erzeugt: " + dropAmount + "x " + selectedDrop.getMaterial() + " bei " + loc);
                        }
                    } else {
                        Bukkit.getLogger().warning("[CoreGenerators] Welt ist null für Drop-Location: " + loc);
                    }
                }
            }

            if (debug) {
                long secondsLeft = Math.max(0, generator.getFuelEndTime() - now);
                Bukkit.getLogger().info("[DEBUG] Generator " + generator.getGeneratorId() +
                        " bei " + generator.getLocation() + " aktiv. Verbleibend: " +
                        secondsLeft + " Sek.");
            }
        }
    }
}
