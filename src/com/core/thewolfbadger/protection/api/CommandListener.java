package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import com.core.thewolfbadger.protection.main.ProtectionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandListener implements CommandExecutor, TabCompleter {
    private static final String[] SUBCOMMANDS = new String[] {"wand", "setspawn", "cancel", "complete", "info", "reload"};

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("prot")) {
            return onProt(sender, args);
        }
        if (cmd.getName().equalsIgnoreCase("spawn")) {
            return onSpawn(sender);
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("prot") || !Main.getMain().isAdmin(sender)) {
            return Collections.emptyList();
        }
        if (args.length != 1) {
            return Collections.emptyList();
        }
        String prefix = args[0].toLowerCase();
        List<String> matches = new ArrayList<String>();
        for (int i = 0; i < SUBCOMMANDS.length; i++) {
            if (SUBCOMMANDS[i].startsWith(prefix)) {
                matches.add(SUBCOMMANDS[i]);
            }
        }
        return matches;
    }

    private boolean onProt(CommandSender sender, String[] args) {
        Main plugin = Main.getMain();
        if (!plugin.isAdmin(sender)) {
            plugin.send(sender, "Messages.NoPermission");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("wand")) {
            giveWand(sender);
        } else if (sub.equals("setspawn")) {
            setSpawn(sender);
        } else if (sub.equals("cancel")) {
            cancelSelection(sender);
        } else if (sub.equals("complete")) {
            completeSelection(sender);
        } else if (sub.equals("info")) {
            sendInfo(sender);
        } else if (sub.equals("reload")) {
            plugin.reloadProtection();
            plugin.send(sender, "Messages.Reloaded");
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try /prot for help.");
        }
        return true;
    }

    private boolean onSpawn(CommandSender sender) {
        Main plugin = Main.getMain();
        if (!(sender instanceof Player)) {
            plugin.send(sender, "Messages.PlayersOnly");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("badgerprotection.spawn")) {
            plugin.send(player, "Messages.NoPermission");
            return true;
        }
        if (!API.getAPI().isReady()) {
            plugin.send(player, "Messages.NotReady");
            return true;
        }
        ProtectionManager manager = plugin.getManager();
        if (manager.isWarming(player)) {
            plugin.send(player, "Messages.AlreadyTeleporting");
            return true;
        }
        if (hasNearbyPlayers(player) && plugin.teleportDelay() > 0) {
            plugin.send(player, "Messages.PlayersNearby", "{DELAY}", String.valueOf(plugin.teleportDelay()));
            manager.startWarmup(player);
            return true;
        }
        manager.arriveAtSpawn(player);
        return true;
    }

    private void giveWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Main.getMain().send(sender, "Messages.PlayersOnly");
            return;
        }
        Player player = (Player) sender;
        API regions = API.getAPI();
        if (regions.isSelecting(player.getUniqueId()) && BlockSelectionMode.hasWand(player)) {
            player.sendMessage(ChatColor.RED + "You are already selecting a BadgerProtection region.");
            return;
        }
        if (!BlockSelectionMode.hasWand(player) && player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your inventory is full.");
            return;
        }
        BlockSelectionMode.removeWands(player);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(BlockSelectionMode.createWand());
        if (!leftover.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Your inventory is full.");
            return;
        }
        regions.startSelection(player.getUniqueId());
        player.sendMessage(ChatColor.GOLD + "You received the " + ChatColor.YELLOW + "Protection Wand" + ChatColor.GOLD + ".");
        player.sendMessage(ChatColor.GRAY + "Left-click sets the first corner. Right-click sets the second.");
        player.sendMessage(ChatColor.GRAY + "Then run /prot complete, or /prot cancel to put the wand away.");
    }

    private void setSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Main.getMain().send(sender, "Messages.PlayersOnly");
            return;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        API.getAPI().setSpawn(location);
        player.sendMessage(ChatColor.GOLD + "Spawn point set for " + ChatColor.YELLOW + "BadgerProtection" + ChatColor.GOLD + ".");
        if (API.getAPI().hasRegion() && !API.getAPI().getCuboid().contains(location)) {
            player.sendMessage(ChatColor.RED + "That spot is outside the protected region, so players who warp here will not be protected.");
        }
    }

    private void cancelSelection(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Main.getMain().send(sender, "Messages.PlayersOnly");
            return;
        }
        Player player = (Player) sender;
        if (!API.getAPI().isSelecting(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not selecting a region.");
            return;
        }
        API.getAPI().clearSelection(player.getUniqueId());
        BlockSelectionMode.removeWands(player);
        player.sendMessage(ChatColor.GOLD + "Selection cancelled.");
    }

    private void completeSelection(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Main.getMain().send(sender, "Messages.PlayersOnly");
            return;
        }
        Player player = (Player) sender;
        API regions = API.getAPI();
        UUID id = player.getUniqueId();
        if (!regions.isSelecting(id)) {
            player.sendMessage(ChatColor.RED + "You are not selecting a region. Run /prot wand first.");
            return;
        }
        if (!regions.hasBoth(id)) {
            player.sendMessage(ChatColor.RED + "Select both corners before completing the region.");
            return;
        }
        if (!regions.sameWorld(id)) {
            player.sendMessage(ChatColor.RED + "Both corners have to be in the same world.");
            return;
        }
        if (!regions.saveRegion(regions.getFirst(id), regions.getSecond(id))) {
            player.sendMessage(ChatColor.RED + "Could not save that region.");
            return;
        }
        Cuboid region = regions.getCuboid();
        regions.clearSelection(id);
        BlockSelectionMode.removeWands(player);
        player.sendMessage(ChatColor.GOLD + "Spawn region saved (" + region.getVolume() + " blocks).");
        if (regions.hasSpawn() && !region.contains(regions.getSpawn())) {
            player.sendMessage(ChatColor.RED + "The /spawn point is outside this region. Stand inside it and run /prot setspawn.");
        } else if (!regions.hasSpawn()) {
            player.sendMessage(ChatColor.RED + "Set the warp point with /prot setspawn before players can use /spawn.");
        }
        Main.getMain().grantPlayersInside();
    }

    private void sendInfo(CommandSender sender) {
        API regions = API.getAPI();
        sender.sendMessage(ChatColor.GOLD + "BadgerProtection");
        if (regions.hasRegion()) {
            Cuboid region = regions.getCuboid();
            sender.sendMessage(ChatColor.GRAY + "Region: " + ChatColor.WHITE + region.getWorldName()
                    + " (" + region.getLowerX() + ", " + region.getLowerY() + ", " + region.getLowerZ()
                    + ") to (" + region.getUpperX() + ", " + region.getUpperY() + ", " + region.getUpperZ() + ")");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Region: " + ChatColor.RED + "not set");
        }
        if (regions.hasSpawn()) {
            Location spawn = regions.getSpawn();
            sender.sendMessage(ChatColor.GRAY + "Spawn: " + ChatColor.WHITE + spawn.getWorld().getName()
                    + " " + formatCoord(spawn.getX()) + ", " + formatCoord(spawn.getY()) + ", " + formatCoord(spawn.getZ()));
        } else {
            sender.sendMessage(ChatColor.GRAY + "Spawn: " + ChatColor.RED + "not set");
        }
        sender.sendMessage(ChatColor.GRAY + "/spawn delay: " + ChatColor.WHITE + Main.getMain().teleportDelay() + "s"
                + ChatColor.GRAY + "  radius: " + ChatColor.WHITE + (int) Main.getMain().checkRadius());
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String state = Main.getMain().getManager().isProtected(player) ? ChatColor.GREEN + "protected" : ChatColor.RED + "unprotected";
            sender.sendMessage(ChatColor.GRAY + "You are " + state + ChatColor.GRAY + ". Walking back into spawn does not restore this.");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "BadgerProtection");
        sender.sendMessage(ChatColor.YELLOW + "/prot wand" + ChatColor.GRAY + " - Select the two corners of spawn");
        sender.sendMessage(ChatColor.YELLOW + "/prot setspawn" + ChatColor.GRAY + " - Set the /spawn warp to where you stand");
        sender.sendMessage(ChatColor.YELLOW + "/prot complete" + ChatColor.GRAY + " - Save the selected region");
        sender.sendMessage(ChatColor.YELLOW + "/prot cancel" + ChatColor.GRAY + " - Leave selection mode");
        sender.sendMessage(ChatColor.YELLOW + "/prot info" + ChatColor.GRAY + " - Show the region, spawn, and your status");
        sender.sendMessage(ChatColor.YELLOW + "/prot reload" + ChatColor.GRAY + " - Reload config.yml");
        sender.sendMessage(ChatColor.YELLOW + "/spawn" + ChatColor.GRAY + " - Warp to spawn and regain protection. Nearby players add a wait. Moving cancels it.");
    }

    private boolean hasNearbyPlayers(Player player) {
        double radius = Main.getMain().checkRadius();
        if (radius <= 0D) {
            return false;
        }
        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        for (int i = 0; i < nearby.size(); i++) {
            Entity entity = nearby.get(i);
            if (!(entity instanceof Player)) {
                continue;
            }
            Player other = (Player) entity;
            if (!other.isOnline() || other.isDead()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String formatCoord(double value) {
        return String.valueOf(Math.round(value * 10D) / 10D);
    }
}
