package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/18/14
 * Time: 12:42 PM
 */
public class BlockSelectionMode implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(Main.getMain().usingWand.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            if(e.getPlayer().getItemInHand().getType().equals(Material.BONE)) {
                if(e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    e.getPlayer().sendMessage(ChatColor.GOLD+"First position has been set for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+"!");
                    API.getAPI().setLoc1(e.getBlock().getLocation());
                    if(API.getAPI().areSet()) {
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"The two positions for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" are set!");
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"You are ready to type '/prot complete'!");
                    }
                }
            }
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(Main.getMain().usingWand.contains(e.getPlayer().getUniqueId())) {
            if(e.getPlayer().getItemInHand().getType().equals(Material.BONE)) {
            if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if(e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    e.getPlayer().sendMessage(ChatColor.GOLD+"First position has been set for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+"!");
                    API.getAPI().setLoc1(e.getClickedBlock().getLocation());
                    if(API.getAPI().areSet()) {
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"The two positions for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" are set!");
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"You are ready to type '/prot complete'!");
                    }
                }
            } else
                if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    e.getPlayer().sendMessage(ChatColor.GOLD+"Second position has been set for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+"!");
                    API.getAPI().setLoc2(e.getClickedBlock().getLocation());
                    if(API.getAPI().areSet()) {
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"The two positions for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" are set!");
                        e.getPlayer().sendMessage("");
                        e.getPlayer().sendMessage(ChatColor.GOLD+"You are ready to type '/prot complete'!");
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(Main.getMain().usingWand.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED+"You can not do that in "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.RED+" selection mode!");
        }
    }
}
