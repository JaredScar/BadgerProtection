package com.core.thewolfbadger.protection.main;

import com.core.thewolfbadger.protection.api.API;
import com.core.thewolfbadger.protection.api.BlockSelectionMode;
import com.core.thewolfbadger.protection.api.CommandListener;
import com.core.thewolfbadger.protection.api.Cuboid;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/18/14
 * Time: 10:49 AM
 */
public class Main extends JavaPlugin implements Listener {
    public static Main m;
    public ArrayList<UUID> hasProt = new ArrayList<UUID>();
    public ArrayList<UUID> teleporting = new ArrayList<UUID>();
    public ArrayList<UUID> usingWand = new ArrayList<UUID>();
    CommandListener cl;
    public void onEnable() {
        m = this;
        this.cl = new CommandListener();
        getCommand("prot").setExecutor(this.cl);
        getCommand("spawn").setExecutor(this.cl);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new BlockSelectionMode(), this);
    }
    public void onDisable() {}
    public static Main getMain() {
        return m;
    }
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(API.getAPI().allSet()) {
        Cuboid cube = API.getAPI().getCuboid();
        Player p = e.getPlayer();
        Location from = e.getFrom();
        Location to = e.getTo();
        if(teleporting.contains(p.getUniqueId())) {
            if(to.getX() > from.getX() || to.getZ() > from.getZ() || to.getY() > from.getY()) {
                teleporting.remove(p.getUniqueId());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.TeleportCancelled")));
            }
        }
        if(hasProt.contains(p.getUniqueId())) {
            if(!cube.isInCuboid(p)) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.LostProtection")));
                hasProt.remove(p.getUniqueId());
            }
        }
        }
    }
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if(API.getAPI().allSet()) {
        Location from = e.getFrom();
        Location to = e.getTo();
        Cuboid cube = API.getAPI().getCuboid();
        Player p = e.getPlayer();
        if(hasProt.contains(p.getUniqueId())) {
            if(!cube.contains(to)) {
            hasProt.remove(p.getUniqueId());
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.LostProtection")));
            }
        }
        if(!hasProt.contains(p.getUniqueId())) {
            if(cube.contains(to)) {
                hasProt.add(p.getUniqueId());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.GainProtection")));
                }
            }
        }
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(API.getAPI().allSet()) {
        Player p = e.getPlayer();
        Location l = e.getBlock().getLocation();
        Cuboid cube = API.getAPI().getCuboid();
        if(cube.contains(l) && !p.hasPermission("BadgerProtection.Admin")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.PreventPlace")));
            }
        }
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(API.getAPI().allSet()) {
        Player p = e.getPlayer();
        Location l = e.getBlock().getLocation();
        Cuboid cube = API.getAPI().getCuboid();
        if(cube.contains(l) && !p.hasPermission("BadgerProtection.Admin")) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.PreventBreak")));
            }
        }
    }
    @EventHandler
    public void onDam(EntityDamageByEntityEvent e) {
        if(API.getAPI().allSet()) {
        Entity ent = e.getEntity();
        Entity damager = e.getDamager();
        if(ent instanceof Player && damager instanceof Player) {
            Player p = (Player) ent;
            Player damageer = (Player) damager;
            if(hasProt.contains(p.getUniqueId())) {
                damageer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.PlayerHasProtection").replace("{PLAYER}", p.getName())));
            } else
                if(hasProt.contains(damageer.getUniqueId()) && !hasProt.contains(p.getUniqueId())) {
                    hasProt.remove(damageer.getUniqueId());
                    damageer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Messages.LostProtection")));
                }
            }
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(API.getAPI().allSet()) {
        Player p = e.getPlayer();
        Cuboid cube = API.getAPI().getCuboid();
        if(cube.isInCuboid(p)) {
            hasProt.add(p.getUniqueId());
        }
        p.sendMessage(ChatColor.GOLD+"This server is using "+ChatColor.GRAY+"{"+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GRAY+"}"+ChatColor.GOLD+" as their Spawn Protection --- created by TheWolfBadger (Bukkit).");
        }
    }
    @EventHandler
    public void onDam(EntityDamageEvent e) {
        if(API.getAPI().allSet()) {
        Entity damaged = e.getEntity();
        EntityDamageEvent.DamageCause cause = e.getCause();
        if(damaged instanceof Player) {
            Player p = (Player) damaged;
            if(hasProt.contains(p.getUniqueId())) {
                e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if(API.getAPI().allSet()) {
        Entity ent = e.getEntity();
        Cuboid cube = API.getAPI().getCuboid();
        List<Block> blocksAffect = e.blockList();
        for(Block blocks : blocksAffect) {
            Location chek = blocks.getLocation();
            if(cube.contains(chek)) {
                e.setCancelled(true);
                break;
                }
            }
        }
    }
}
