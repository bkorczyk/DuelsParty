package org.antix.duelsparty.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record ArenaLocation(String worldName, double x, double y, double z, float yaw, float pitch) {

    public static ArenaLocation fromLocation(Location loc) {
        return new ArenaLocation(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String serialize() {
        // Używamy Locale.US, aby wymusić kropkę jako separator dziesiętny niezależnie od kraju
        return String.format(java.util.Locale.US, "%s;%f;%f;%f;%f;%f", worldName, x, y, z, yaw, pitch);
    }

    public static ArenaLocation deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.replace(',', '.').split(";");
        return new ArenaLocation(
                parts[0],
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }
}