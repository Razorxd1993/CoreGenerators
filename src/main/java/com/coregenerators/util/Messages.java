package com.coregenerators.util;

import com.coregenerators.main.CoreGenerators;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {

    private static YamlConfiguration config;

    public static void load() {
        File file = new File(CoreGenerators.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) {
            CoreGenerators.getInstance().saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static String get(String path) {
        if (config == null) return "§c[Nachrichten nicht geladen]";
        String prefix = config.getString("prefix", "");
        String message = config.getString(path, "§c[Fehlende Nachricht: " + path + "]");
        return message.replace("%prefix%", prefix);
    }

    public static String get(String path, Object... replacements) {
        String msg = get(path);
        for (int i = 0; i < replacements.length; i += 2) {
            msg = msg.replace(replacements[i].toString(), replacements[i + 1].toString());
        }
        return msg;
    }

    public static YamlConfiguration getConfig() {
        return config;
    }
}
