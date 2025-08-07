package com.coregenerators.listener;

import com.coregenerators.main.CoreGenerators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputListener implements Listener {

    private final CoreGenerators plugin;

    // Map mit Spielern, die eine Eingabe erwartet, und zugehörigem Callback
    private final Map<UUID, InputCallback> waitingForInput = new HashMap<>();

    public ChatInputListener(CoreGenerators plugin) {
        this.plugin = plugin;
    }

    /**
     * Wartet auf die Chat-Eingabe des Spielers und ruft den Callback auf.
     */
    public void waitForInput(Player player, InputCallback callback) {
        waitingForInput.put(player.getUniqueId(), callback);
        player.sendMessage(ChatColor.YELLOW + "Bitte gib den Spielernamen im Chat ein (oder tippe 'abbrechen').");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!waitingForInput.containsKey(uuid)) {
            return; // Spieler erwartet keine Eingabe
        }

        event.setCancelled(true); // Nachricht wird nicht normal gesendet

        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("abbrechen")) {
            waitingForInput.remove(uuid);
            player.sendMessage(ChatColor.RED + "Eingabe abgebrochen.");
            return;
        }

        InputCallback callback = waitingForInput.get(uuid);
        boolean success = callback.onInput(message);

        if (success) {
            // Wenn Eingabe erfolgreich, aus der Map entfernen
            waitingForInput.remove(uuid);
        } else {
            // Wenn nicht erfolgreich, kann Spieler neu eingeben (bleibt in Map)
            player.sendMessage(ChatColor.RED + "Ungültige Eingabe, versuche es erneut.");
        }
    }

    /**
     * Interface für Callback bei Chat-Eingaben.
     */
    public interface InputCallback {
        /**
         * @param input der eingegebene Text, null bei Abbruch
         * @return true wenn Eingabe gültig und verarbeitet wurde, false sonst (z.B. Fehler)
         */
        boolean onInput(String input);
    }
}
