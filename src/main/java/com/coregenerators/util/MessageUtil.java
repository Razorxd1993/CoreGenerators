package com.coregenerators.util;

import org.bukkit.entity.Player;

public class MessageUtil {

    public static void send(Player player, String key) {
        player.sendMessage(Messages.get(key).replace("&", "§"));
    }

    public static void send(Player player, String key, Object... replacements) {
        player.sendMessage(Messages.get(key, replacements).replace("&", "§"));
    }
}
