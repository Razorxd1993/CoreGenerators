package com.coregenerators.util;

import org.bukkit.NamespacedKey;
import com.coregenerators.main.CoreGenerators;

public class CoreNamespacedKeys {

    public static final NamespacedKey GENERATOR_ID =
            new NamespacedKey(CoreGenerators.getInstance(), "generator_id");

    public static final NamespacedKey UPGRADE_LEVEL =
            new NamespacedKey(CoreGenerators.getInstance(), "upgrade_level");

    public static final NamespacedKey FUEL_TIME =
            new NamespacedKey(CoreGenerators.getInstance(), "fuel_time");

    private CoreNamespacedKeys() {
        // Utility class – kein Konstruktor erlaubt
    }
}
