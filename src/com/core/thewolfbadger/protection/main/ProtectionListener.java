package com.core.thewolfbadger.protection.main;

import com.core.thewolfbadger.protection.api.API;
import com.core.thewolfbadger.protection.api.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Iterator;

/**
 * jProtection / MCPVP rules: protection is lost by leaving spawn and is not
 * restored by walking back in. Only /spawn and respawn give it back.
 */
public class ProtectionListener implements Listener {
    private final Main plugin;

    public ProtectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getWorld() == null || to == null || to.getWorld() == null) {
            return;
        }
        Player player = event.getPlayer();
        if (moved(from, to)) {
            plugin.getManager().cancelWarmup(player, true);
        }
        if (!ready() || sameBlock(from, to)) {
            return;
        }
        if (plugin.getManager().isProtected(player) && !API.getAPI().getCuboid().contains(to)) {
            plugin.getManager().setProtected(player, false, true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!ready()) {
            return;
        }
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.getManager().isProtected(player) && !API.getAPI().getCuboid().contains(to)) {
            plugin.getManager().setProtected(player, false, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!ready() || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        Player attacker = null;
        if (event instanceof EntityDamageByEntityEvent) {
            attacker = resolveAttacker(((EntityDamageByEntityEvent) event).getDamager());
            if (attacker != null && attacker.getUniqueId().equals(victim.getUniqueId())) {
                attacker = null;
            }
        }
        ProtectionManager manager = plugin.getManager();
        if (manager.isProtected(victim)) {
            event.setCancelled(true);
            if (attacker != null) {
                manager.notifyProtected(attacker, victim);
            }
            return;
        }
        if (event.isCancelled() || attacker == null || !manager.isProtected(attacker)) {
            return;
        }
        manager.setProtected(attacker, false, true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (denyBuild(event.getPlayer(), event.getBlock().getLocation(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (denyBuild(event.getPlayer(), event.getBlock().getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBlockClicked() == null || event.getBlockFace() == null) {
            return;
        }
        Block liquid = event.getBlockClicked().getRelative(event.getBlockFace());
        if (denyBuild(event.getPlayer(), liquid.getLocation(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (event.getBlockClicked() == null) {
            return;
        }
        if (denyBuild(event.getPlayer(), event.getBlockClicked().getLocation(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!ready()) {
            return;
        }
        Cuboid region = API.getAPI().getCuboid();
        Iterator<Block> blocks = event.blockList().iterator();
        while (blocks.hasNext()) {
            if (region.contains(blocks.next().getLocation())) {
                blocks.remove();
            }
        }
    }

    @EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getManager().cancelWarmup(player, false);
        plugin.getManager().setProtected(player, false, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!ready()) {
            return;
        }
        Location spawn = API.getAPI().getSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
        boolean inside = spawn != null && API.getAPI().getCuboid().contains(spawn);
        plugin.getManager().setProtected(event.getPlayer(), inside, inside);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!ready()) {
            return;
        }
        Player player = event.getPlayer();
        if (API.getAPI().getCuboid().contains(player.getLocation())) {
            plugin.getManager().setProtected(player, true, false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getManager().clearPlayer(event.getPlayer());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        boolean wasReady = API.getAPI().isReady();
        API.getAPI().loadCache();
        if (!wasReady && API.getAPI().isReady()) {
            plugin.getManager().grantEveryoneInside();
            plugin.getLogger().info("Spawn region loaded with world '" + event.getWorld().getName() + "'.");
        }
    }

    private boolean denyBuild(Player player, Location location, boolean placing) {
        if (!inRegion(location) || plugin.isAdmin(player)) {
            return false;
        }
        plugin.send(player, placing ? "Messages.PreventPlace" : "Messages.PreventBreak");
        return true;
    }

    private boolean inRegion(Location location) {
        return ready() && location != null && API.getAPI().getCuboid().contains(location);
    }

    private boolean ready() {
        return API.getAPI().isReady();
    }

    private boolean moved(Location from, Location to) {
        if (from.getWorld() == null || to.getWorld() == null) {
            return true;
        }
        return !from.getWorld().getName().equals(to.getWorld().getName())
                || Double.doubleToLongBits(from.getX()) != Double.doubleToLongBits(to.getX())
                || Double.doubleToLongBits(from.getY()) != Double.doubleToLongBits(to.getY())
                || Double.doubleToLongBits(from.getZ()) != Double.doubleToLongBits(to.getZ());
    }

    private boolean sameBlock(Location from, Location to) {
        if (from.getWorld() == null || to.getWorld() == null) {
            return false;
        }
        return from.getWorld().getName().equals(to.getWorld().getName())
                && from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ();
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        if (damager instanceof TNTPrimed) {
            Entity source = ((TNTPrimed) damager).getSource();
            if (source instanceof Player) {
                return (Player) source;
            }
        }
        return null;
    }
}
