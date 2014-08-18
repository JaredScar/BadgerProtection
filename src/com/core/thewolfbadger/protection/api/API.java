package com.core.thewolfbadger.protection.api;

import com.core.thewolfbadger.protection.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: TheWolfBadger
 * Date: 8/18/14
 * Time: 10:44 AM
 */
public class API {
    public static API api = new API();
    public static API getAPI() {
        return api;
    }
    Location loc1;
    Location loc2;
    public void setLoc1(Location loc) {
        this.loc1 = loc;
    }
    public void setLoc2(Location loc) {
        this.loc2 = loc;
    }
    public boolean allSet() {
        if(Main.getMain().getConfig().get("CuboidSpawn") !=null) {
            if(Main.getMain().getConfig().get("SpawnLoc") !=null) {
                return true;
            }
        }
        return false;
    }
    public void setSpawn(Location spawn) {
        Main.getMain().getConfig().set("SpawnLoc", locToString(spawn));
        Main.getMain().saveConfig();
    }
    public Location getSpawn() {
        String s = Main.getMain().getConfig().getString("SpawnLoc");
        Location l = stringToLoc(s);
        return l;
    }
    public String locToString(Location loc) {
        String location = loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
        return location;
    }
        public Location stringToLoc(String string){
            String[] loc = string.split(";");
            World world = Bukkit.getWorld(loc[0]);
            Double x = Double.parseDouble(loc[1]);
            Double y = Double.parseDouble(loc[2]);
            Double z = Double.parseDouble(loc[3]);
            Float yaw = Float.parseFloat(loc[4]);
            Float pitch = Float.parseFloat(loc[5]);

            return new Location(world, x, y, z, yaw, pitch);
        }
    public boolean areSet() {
        if(this.loc1 !=null) {
            if(this.loc2 !=null) {
                return true;
            }
        }
        return false;
    }
    public void complete() {
        if(areSet()) {
            Cuboid cube = new Cuboid(loc1, loc2);
            Main.getMain().getConfig().set("CuboidSpawn.Loc1", locToString(cube.getLoc1()));
            Main.getMain().getConfig().set("CuboidSpawn.Loc2", locToString(cube.getLoc2()));
            Main.getMain().saveConfig();
            this.loc1 = null;
            this.loc2 = null;
        }
    }
    public Cuboid getCuboid() {
        Cuboid cube = new Cuboid(stringToLoc(Main.getMain().getConfig().getString("CuboidSpawn.Loc1")), stringToLoc(Main.getMain().getConfig().getString("CuboidSpawn.Loc2")));
        return cube;
    }
}
