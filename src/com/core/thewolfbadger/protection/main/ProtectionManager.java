package com.core.thewolfbadger.protection.main;

import com.core.thewolfbadger.protection.api.API;
import com.core.thewolfbadger.protection.api.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Spawn protection is a flag. Leaving spawn, or hitting someone who does not have it, removes it.
 * Walking back in does not restore it. /spawn does.
 */
public class ProtectionManager {
    private final Main plugin;
    private final Set<UUID> protectedPlayers = new HashSet<UUID>();
    private final Map<UUID, BukkitTask> warmups = new HashMap<UUID, BukkitTask>();
    private final Map<UUID, Long> protectionNoticeAt = new HashMap<UUID, Long>();

    public ProtectionManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean isProtected(Player player) {
        return player != null && protectedPlayers.contains(player.getUniqueId());
    }

    public void setProtected(Player player, boolean protect, boolean notify) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        boolean currently = protectedPlayers.contains(id);
        if (protect == currently) {
            return;
        }
        if (protect) {
            protectedPlayers.add(id);
            if (notify) {
                plugin.send(player, "Messages.GainProtection");
            }
        } else {
            protectedPlayers.remove(id);
            if (notify) {
                plugin.send(player, "Messages.LostProtection");
            }
        }
    }

    public void grantEveryoneInside() {
        if (!API.getAPI().isReady()) {
            return;
        }
        Cuboid region = API.getAPI().getCuboid();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (region.contains(player.getLocation())) {
                setProtected(player, true, false);
            }
        }
    }

    public void dropProtectedPlayersOutside() {
        if (!API.getAPI().isReady()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setProtected(player, false, false);
            }
            return;
        }
        Cuboid region = API.getAPI().getCuboid();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isProtected(player) && !region.contains(player.getLocation())) {
                setProtected(player, false, true);
            }
        }
    }

    public void arriveAtSpawn(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return;
        }
        Location spawn = API.getAPI().getSpawn();
        if (spawn == null) {
            return;
        }
        if (!player.teleport(spawn)) {
            return;
        }
        if (API.getAPI().hasRegion() && API.getAPI().getCuboid().contains(spawn)) {
            setProtected(player, true, true);
        }
    }

    public boolean isWarming(Player player) {
        return player != null && warmups.containsKey(player.getUniqueId());
    }

    public void startWarmup(final Player player) {
        cancelWarmup(player, false);
        final int delay = plugin.teleportDelay();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                warmups.remove(player.getUniqueId());
                arriveAtSpawn(player);
            }
        }, delay * 20L);
        warmups.put(player.getUniqueId(), task);
    }

    public void cancelWarmup(Player player, boolean notify) {
        if (player == null) {
            return;
        }
        BukkitTask task = warmups.remove(player.getUniqueId());
        if (task == null) {
            return;
        }
        task.cancel();
        if (notify && player.isOnline()) {
            plugin.send(player, "Messages.TeleportCancelled");
        }
    }

    public void notifyProtected(Player attacker, Player victim) {
        if (attacker == null || victim == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = protectionNoticeAt.get(attacker.getUniqueId());
        if (last != null && now - last.longValue() < 1500L) {
            return;
        }
        protectionNoticeAt.put(attacker.getUniqueId(), Long.valueOf(now));
        plugin.send(attacker, "Messages.PlayerHasProtection", "{PLAYER}", victim.getName());
    }

    public void clearPlayer(Player player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        cancelWarmup(player, false);
        protectedPlayers.remove(id);
        protectionNoticeAt.remove(id);
        API.getAPI().clearSelection(id);
    }

    public void shutdown() {
        for (BukkitTask task : warmups.values()) {
            task.cancel();
        }
        warmups.clear();
        protectedPlayers.clear();
        protectionNoticeAt.clear();
    }
}
