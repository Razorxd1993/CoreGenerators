package com.coregenerators.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.Clipboard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FaweCutUtil {

    public static void cutRegion(Player player, Location minLoc, Location maxLoc) {
        try {
            World weWorld = BukkitAdapter.adapt(minLoc.getWorld());

            BlockVector3 min = BlockVector3.at(
                    Math.min(minLoc.getBlockX(), maxLoc.getBlockX()),
                    Math.min(minLoc.getBlockY(), maxLoc.getBlockY()),
                    Math.min(minLoc.getBlockZ(), maxLoc.getBlockZ())
            );

            BlockVector3 max = BlockVector3.at(
                    Math.max(minLoc.getBlockX(), maxLoc.getBlockX()),
                    Math.max(minLoc.getBlockY(), maxLoc.getBlockY()),
                    Math.max(minLoc.getBlockZ(), maxLoc.getBlockZ())
            );

            CuboidRegion region = new CuboidRegion(weWorld, min, max);

            try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
                session.setBlocks(region, new BlockPattern(BlockTypes.AIR.getDefaultState()));
                Operations.complete(session.commit());
            }

            player.sendMessage("§aRegion erfolgreich ausgeschnitten.");

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cFehler beim Ausschneiden: " + e.getMessage());
        }
    }
}
