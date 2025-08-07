package com.coregenerators.generatorconfigs;

import com.coregenerators.generatorconfigs.PlacedGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class MemberGui {

    public static void open(Player player, PlacedGenerator generator) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Mitglieder");

        // Mitgliederköpfe
        int slot = 0;
        for (UUID memberId : generator.getMembers()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                meta.setDisplayName("§e" + offlinePlayer.getName());
                meta.setLore(java.util.List.of("§7Klicke zum Entfernen"));
                skull.setItemMeta(meta);
            }
            inv.setItem(slot++, skull);
        }

        // Hinzufügen-Button (grüner Farbstoff)
        ItemStack addItem = new ItemStack(Material.LIME_DYE);
        var addMeta = addItem.getItemMeta();
        addMeta.setDisplayName("§aMitglied hinzufügen");
        addMeta.setLore(java.util.List.of("§7Klicke, um einen Spieler hinzuzufügen"));
        addItem.setItemMeta(addMeta);
        inv.setItem(26, addItem);

        player.openInventory(inv);
    }
}