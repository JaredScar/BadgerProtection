package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/18/14
 * Time: 12:04 PM
 */
public class CommandListener implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if(cmd.getName().equalsIgnoreCase("prot")) {
            if(sender.hasPermission("BadgerProtection.Admin")) {
            switch (args.length) {
                case 0:
                    //Help Menu
                    sender.sendMessage(ChatColor.GOLD+"./prot wand - Go into "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" selection mode...");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GOLD+"./prot setspawn - Set the spawn for the command /spawn...");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GOLD+"./prot cancel - Exit the "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" selection mode...");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GOLD+"./prot complete - Complete the "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" selection mode if you set the positions...");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GOLD+"./spawn - Teleport to the "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" set spawn!");
                    break;
                case 1:
                    if(args[0].equalsIgnoreCase("wand")) {
                        //Wand will be a bone
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            if(!Main.getMain().usingWand.contains(p.getUniqueId())) {
                            p.getInventory().addItem(new ItemStack(Material.BONE));
                            p.updateInventory();
                                Main.getMain().usingWand.add(p.getUniqueId());
                            p.sendMessage(ChatColor.GOLD+"You have been given "+ChatColor.YELLOW+"TheWolfBadger"+ChatColor.GOLD+" Bone of selecting!");
                            p.sendMessage("");
                            p.sendMessage(ChatColor.GRAY+"Select two points with right-clicking and left-clicking and type '/prot complete' when you have selected both points...");
                            p.sendMessage("");
                            p.sendMessage(ChatColor.GRAY+"At any given point in time, type '/prot cancel' to exit selection mode...");
                            } else {
                                p.sendMessage(ChatColor.RED+"You are already in "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.RED+" selection mode!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED+"You must be a player to run this command!");
                        }
                    } else
                    if(args[0].equalsIgnoreCase("setspawn")) {
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            API.getAPI().setSpawn(p.getLocation());
                            p.sendMessage(ChatColor.GOLD+"The spawn for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" has been set!");
                        } else {
                            sender.sendMessage(ChatColor.RED+"You must be a player to run this command!");
                        }
                    } else
                    if(args[0].equalsIgnoreCase("cancel")) {
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            if(Main.getMain().usingWand.contains(p.getUniqueId())) {
                                p.getInventory().remove(Material.BONE);
                                Main.getMain().usingWand.remove(p.getUniqueId());
                                p.sendMessage(ChatColor.GOLD+"You have been removed from "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+" selection mode!");
                            } else {
                                p.sendMessage(ChatColor.RED+"You are currently not in "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.RED+" selection mode!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED+"You must be a player to run this command!");
                        }
                    } else
                    if(args[0].equalsIgnoreCase("complete")) {
                        if(sender instanceof Player) {
                            Player p = (Player) sender;
                            if(Main.getMain().usingWand.contains(p.getUniqueId())) {
                                p.getInventory().remove(Material.BONE);
                                Main.getMain().usingWand.remove(p.getUniqueId());
                                if(API.getAPI().areSet()) {
                                    API.getAPI().complete();
                                    p.sendMessage(ChatColor.GOLD+"You have set the spawn area for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.GOLD+"!");
                                } else {
                                    p.sendMessage(ChatColor.RED+"You have not set the spawn area for "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.RED+" correctly!");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED+"You must be in "+ChatColor.YELLOW+"BadgerProtection"+ChatColor.RED+" selection mode to do this!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED+"You must be a player to run this command!");
                        }
                    }
                    break;
                }
            }
        } else
        if(cmd.getName().equalsIgnoreCase("spawn")) {
            if(API.getAPI().allSet()) {
            if(sender instanceof Player) {
                final Player p = (Player) sender;
                if(!Main.getMain().teleporting.contains(p.getUniqueId())) {
                    boolean bool = false;
                    for(Entity ents : p.getNearbyEntities(Main.getMain().getConfig().getDouble("CheckRadius"), Main.getMain().getConfig().getDouble("CheckRadius"), Main.getMain().getConfig().getDouble("CheckRadius"))) {
                        if(ents instanceof Player) {
                            bool = true;
                            break;
                        }
                    }
                    if(!bool) {
                        p.teleport(API.getAPI().getSpawn());
                    } else {
                        Main.getMain().teleporting.add(p.getUniqueId());
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.getMain().getConfig().getString("Messages.PlayersNearby").replace("{DELAY}", String.valueOf(Main.getMain().getConfig().getLong("TeleportDelay")))));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(Main.getMain().teleporting.contains(p.getUniqueId())) {
                                    p.teleport(API.getAPI().getSpawn());
                                    }
                                }
                            }.runTaskLater(Main.getMain(), Main.getMain().getConfig().getLong("TeleportDelay"));
                        }
                    } else {
                    p.sendMessage(ChatColor.RED+"You are already waiting to be teleported!");
                    }
                } else {
                sender.sendMessage(ChatColor.RED+"You must be a player to run this command!");
                }
            } else {
                sender.sendMessage(ChatColor.RED+"Everything in the config must be set for you to run this command!");
            }
        }
        return true;
    }
}
