package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * Bone wand used to mark the two corners of the spawn region.
 * Left click sets the first corner and right click sets the second.
 */
public class BlockSelectionMode implements Listener {
    public static final String WAND_NAME = ChatColor.YELLOW + "Protection Wand";

    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.BONE, 1);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.BONE || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && WAND_NAME.equals(meta.getDisplayName());
    }

    public static boolean hasWand(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isWand(contents[i])) {
                return true;
            }
        }
        return isWand(player.getInventory().getItemInOffHand());
    }

    public static void removeWands(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isWand(contents[i])) {
                player.getInventory().setItem(i, null);
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (isWand(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent event) {
        if (!holdingWand(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        mark(event.getPlayer(), event.getBlock().getLocation(), true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (!holdingWand(event.getPlayer())) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        event.setCancelled(true);
        mark(event.getPlayer(), event.getClickedBlock().getLocation(), event.getAction() == Action.LEFT_CLICK_BLOCK);
    }

    private boolean holdingWand(Player player) {
        if (!API.getAPI().isSelecting(player.getUniqueId())) {
            return false;
        }
        if (isWand(player.getInventory().getItemInMainHand())) {
            return true;
        }
        return isWand(player.getInventory().getItemInOffHand());
    }

    private void mark(Player player, org.bukkit.Location location, boolean first) {
        API regions = API.getAPI();
        UUID id = player.getUniqueId();
        boolean changed = first ? regions.setFirst(id, location) : regions.setSecond(id, location);
        if (!changed) {
            return;
        }
        if (first) {
            player.sendMessage(ChatColor.GOLD + "First position set for " + ChatColor.YELLOW + "BadgerProtection" + ChatColor.GOLD + ".");
        } else {
            player.sendMessage(ChatColor.GOLD + "Second position set for " + ChatColor.YELLOW + "BadgerProtection" + ChatColor.GOLD + ".");
        }
        if (regions.hasBoth(id)) {
            player.sendMessage(ChatColor.GOLD + "Both corners are set. Type " + ChatColor.YELLOW + "/prot complete" + ChatColor.GOLD + " to save the region.");
        }
    }
}
