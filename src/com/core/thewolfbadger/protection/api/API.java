package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores the spawn point, the protected region, and each admin's wand selection.
 */
public class API {
    private static API api;
    private final Map<UUID, Location> firstPositions = new HashMap<UUID, Location>();
    private final Map<UUID, Location> secondPositions = new HashMap<UUID, Location>();
    private final Map<UUID, Boolean> selecting = new HashMap<UUID, Boolean>();
    private Cuboid cuboid;
    private Location spawn;

    public static void init() {
        api = new API();
        api.loadCache();
    }

    public static API getAPI() {
        return api;
    }

    public void loadCache() {
        Main plugin = Main.getMain();
        cuboid = null;
        spawn = null;
        if (plugin == null) {
            return;
        }
        spawn = stringToLoc(plugin.getConfig().getString("SpawnLoc"));
        Location first = stringToLoc(plugin.getConfig().getString("CuboidSpawn.Loc1"));
        Location second = stringToLoc(plugin.getConfig().getString("CuboidSpawn.Loc2"));
        if (first != null && second != null && first.getWorld() != null && first.getWorld().equals(second.getWorld())) {
            cuboid = new Cuboid(first, second);
        }
    }

    public boolean isReady() {
        return cuboid != null && spawn != null && spawn.getWorld() != null;
    }

    public boolean hasRegion() {
        return cuboid != null;
    }

    public boolean hasSpawn() {
        return spawn != null && spawn.getWorld() != null;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public Location getSpawn() {
        return spawn == null ? null : spawn.clone();
    }

    public void setSpawn(Location location) {
        spawn = location.clone();
        Main.getMain().getConfig().set("SpawnLoc", locToString(spawn));
        Main.getMain().saveConfig();
    }

    public boolean saveRegion(Location first, Location second) {
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        if (!first.getWorld().getName().equals(second.getWorld().getName())) {
            return false;
        }
        cuboid = new Cuboid(first, second);
        Main.getMain().getConfig().set("CuboidSpawn.Loc1", locToString(first));
        Main.getMain().getConfig().set("CuboidSpawn.Loc2", locToString(second));
        Main.getMain().saveConfig();
        return true;
    }

    public void startSelection(UUID id) {
        selecting.put(id, Boolean.TRUE);
    }

    public boolean isSelecting(UUID id) {
        return selecting.containsKey(id);
    }

    public void clearSelection(UUID id) {
        selecting.remove(id);
        firstPositions.remove(id);
        secondPositions.remove(id);
    }

    public boolean setFirst(UUID id, Location location) {
        return set(firstPositions, id, location);
    }

    public boolean setSecond(UUID id, Location location) {
        return set(secondPositions, id, location);
    }

    public boolean hasBoth(UUID id) {
        return firstPositions.containsKey(id) && secondPositions.containsKey(id);
    }

    public Location getFirst(UUID id) {
        return firstPositions.get(id);
    }

    public Location getSecond(UUID id) {
        return secondPositions.get(id);
    }

    public boolean sameWorld(UUID id) {
        Location first = firstPositions.get(id);
        Location second = secondPositions.get(id);
        if (first == null || second == null || first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().getName().equals(second.getWorld().getName());
    }

    private boolean set(Map<UUID, Location> positions, UUID id, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Location block = location.getBlock().getLocation();
        Location existing = positions.get(id);
        positions.put(id, block);
        if (existing == null) {
            return true;
        }
        return existing.getBlockX() != block.getBlockX()
                || existing.getBlockY() != block.getBlockY()
                || existing.getBlockZ() != block.getBlockZ()
                || !existing.getWorld().getName().equals(block.getWorld().getName());
    }

    public String locToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
    }

    public Location stringToLoc(String raw) {
        if (raw == null || raw.length() == 0) {
            return null;
        }
        String[] parts = raw.split(";");
        if (parts.length < 6) {
            warn("Ignoring a saved location because it is incomplete: " + raw);
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException ex) {
            warn("Ignoring a saved location because it is not numeric: " + raw);
            return null;
        }
    }

    private void warn(String message) {
        if (Main.getMain() != null) {
            Main.getMain().getLogger().warning(message);
        }
    }
}
