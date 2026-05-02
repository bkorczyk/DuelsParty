package org.antix.duelsparty.duel;

import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.duel.arena.ArenaLocation; // Pamiętaj o imporcie!
import org.antix.duelsparty.duel.match.MatchState;
import org.bukkit.entity.Player;

public class Duel {
    private final Player player1;
    private final Player player2;
    private final Arena arena;
    private MatchState state = MatchState.STARTING;

    // Konstruktor musi przyjąć graczy, żeby wiedzieć, kogo teleportować
    public Duel(Player player1, Player player2, Arena arena) {
        this.player1 = player1;
        this.player2 = player2;
        this.arena = arena;
    }

    public void start() {
        this.state = MatchState.FIGHTING;
        arena.setBusy(true);

        // Teraz player1 i player2 są już znani klasie
        player1.teleport(toBukkitLocation(arena.getSpawn1()));
        player2.teleport(toBukkitLocation(arena.getSpawn2()));

        player1.sendMessage("§aDuel started against §f" + player2.getName());
        player2.sendMessage("§aDuel started against §f" + player1.getName());
    }

    private org.bukkit.Location toBukkitLocation(ArenaLocation loc) {
        return new org.bukkit.Location(
                org.bukkit.Bukkit.getWorld(loc.worldName()),
                loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch()
        );
    }

    // Gettery
    public Arena getArena() { return arena; }
    public MatchState getState() { return state; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
}