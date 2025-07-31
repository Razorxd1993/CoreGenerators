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
        long now = System.currentTimeMillis() / 1000;

        for (PlacedGenerator generator : CoreGenerators.getInstance().getStorage().getAllGenerators()) {

            if (generator.getFuelEndTime() <= now) {
                if (debug) {
                    Bukkit.getLogger().info("[DEBUG] Generator bei " + generator.getLocation() + " ist leer.");
                }
                continue;
            }

            Generator genData = CoreGenerators.generators.get(generator.getGeneratorId());
            if (genData == null) {
                Bukkit.getLogger().warning("[CoreGenerators] Ungültige Generator-ID: '" + generator.getGeneratorId() + "' bei " + generator.getLocation());
                continue;
            }

            generator.setTickCount(generator.getTickCount() + 1);

            if (generator.getTickCount() >= genData.getInterval()) {
                generator.setTickCount(0);

                for (GeneratorDrop drop : genData.getDrops()) {
                    if (random.nextDouble() <= drop.getChance()) {
                        Location loc = generator.getLocation().clone().add(0.5, 1, 0.5);
                        World world = loc.getWorld();
                        if (world != null) {
                            ItemStack item = new ItemStack(drop.getMaterial(), drop.getAmount());
                            world.dropItemNaturally(loc, item);
                        }
                    }
                }
            }

            if (debug) {
                Bukkit.getLogger().info("[DEBUG] Generator " + generator.getGeneratorId() +
                        " bei " + generator.getLocation() + " ist aktiv. Läuft noch " +
                        (generator.getFuelEndTime() - now) + " Sekunden.");
            }
        }
    }
}
