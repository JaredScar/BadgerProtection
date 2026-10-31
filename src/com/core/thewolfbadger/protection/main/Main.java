package com.core.thewolfbadger.protection.main;

import com.core.thewolfbadger.protection.api.API;
import com.core.thewolfbadger.protection.api.BlockSelectionMode;
import com.core.thewolfbadger.protection.api.CommandListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * jProtection-style MCPVP spawn protection. Leaving spawn drops the flag.
 * Walking back in does not restore it. /spawn does, after a wait if someone is nearby.
 */
public class Main extends JavaPlugin {
    private static Main instance;
    private ProtectionManager manager;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ensureConfig();
        API.init();
        manager = new ProtectionManager(this);
        CommandListener commands = new CommandListener();
        register("prot", commands, commands);
        register("spawn", commands, null);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockSelectionMode(), this);
        if (API.getAPI().isReady()) {
            manager.grantEveryoneInside();
            getLogger().info("Spawn region and spawn point are set.");
        } else if (getConfig().isSet("CuboidSpawn") || getConfig().isSet("SpawnLoc")) {
            getLogger().warning("Spawn data is saved, but its world is not loaded yet. It will be picked up when the world loads.");
        } else {
            getLogger().info("Spawn is not set up yet. Use /prot wand and /prot setspawn.");
        }
    }

    public void onDisable() {
        if (manager != null) {
            manager.shutdown();
        }
        instance = null;
    }

    public void reloadProtection() {
        reloadConfig();
        ensureConfig();
        API.getAPI().loadCache();
        manager.dropProtectedPlayersOutside();
    }

    public static Main getMain() {
        return instance;
    }

    public ProtectionManager getManager() {
        return manager;
    }

    public boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("badgerprotection.admin") || sender.hasPermission("BadgerProtection.Admin");
    }

    public int teleportDelay() {
        return Math.max(0, getConfig().getInt("TeleportDelay", 10));
    }

    public double checkRadius() {
        return Math.max(0D, getConfig().getDouble("CheckRadius", 10D));
    }

    public void send(CommandSender sender, String path) {
        send(sender, path, null, null);
    }

    public void send(CommandSender sender, String path, String token, String value) {
        if (sender == null) {
            return;
        }
        String message = format(path, token, value);
        if (message.length() > 0) {
            sender.sendMessage(message);
        }
    }

    public String format(String path, String token, String value) {
        String raw = getConfig().getString(path);
        if (raw == null || raw.length() == 0) {
            return "";
        }
        String colored = ChatColor.translateAlternateColorCodes('&', raw);
        if (token != null && value != null) {
            colored = colored.replace(token, value);
        }
        return colored;
    }

    private void register(String name, CommandListener executor, CommandListener tab) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command /" + name + " is missing from plugin.yml.");
            return;
        }
        command.setExecutor(executor);
        if (tab != null) {
            command.setTabCompleter(tab);
        }
    }

    private void ensureConfig() {
        boolean dirty = false;
        dirty = setIfMissing("Messages.NoPermission", "&cYou do not have permission to do that.") || dirty;
        dirty = setIfMissing("Messages.NotReady", "&cSpawn protection has not been fully set up yet.") || dirty;
        dirty = setIfMissing("Messages.AlreadyTeleporting", "&cYou are already waiting to be teleported!") || dirty;
        dirty = setIfMissing("Messages.PlayersOnly", "&cYou must be a player to do that.") || dirty;
        dirty = setIfMissing("Messages.Reloaded", "&aBadgerProtection configuration reloaded.") || dirty;
        dirty = replaceExact("Messages.PreventPlace",
                "&cYou can not break a block protected by &eBadgerProtection&c!",
                "&cYou cannot place blocks in &eBadgerProtection&c!") || dirty;
        dirty = replaceExact("Messages.PreventBreak",
                "&cYou can not place in a Cuboid protected by &eBadgerProtection&c!",
                "&cYou cannot break blocks in &eBadgerProtection&c!") || dirty;
        if (dirty) {
            saveConfig();
        }
    }

    private boolean setIfMissing(String path, Object value) {
        if (getConfig().isSet(path)) {
            return false;
        }
        getConfig().set(path, value);
        return true;
    }

    private boolean replaceExact(String path, String previous, String updated) {
        String current = getConfig().getString(path);
        if (current == null || !current.equals(previous)) {
            return false;
        }
        getConfig().set(path, updated);
        return true;
    }

    public void grantPlayersInside() {
        manager.grantEveryoneInside();
    }
}
