package org.antix.duelsparty.duel.arena;

public record ArenaLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
    // Tutaj będziemy mogli później dodać metodę toBukkitLocation(Server server)
}