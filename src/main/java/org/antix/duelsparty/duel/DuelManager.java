package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.arena.Arena;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DuelManager {
    private final List<Arena> arenas = new ArrayList<>();

    public void addArena(Arena arena) {
        arenas.add(arena);
    }

    /**
     * Główna metoda tworzenia pojedynku.
     * Jeśli requestedArena jest null, system sam wylosuje wolną arenę.
     */
    public Duel createDuel(Player player1, Player player2, Arena requestedArena) {
        validatePlayers(player1, player2);

        Arena arena = findAvailableArena(requestedArena);
        arena.setBusy(true);

        return new Duel(arena);
    }

    /**
     * Przeciążenie metody dla szybkich pojedynków (losowa arena).
     */
    public Duel createDuel(Player player1, Player player2) {
        return createDuel(player1, player2, null);
    }

    private void validatePlayers(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            throw new DuelException("error.player-null");
        }
        if (p1.equals(p2)) {
            throw new DuelException("error.self-duel");
        }
    }

    private Arena findAvailableArena(Arena requested) {
        if (requested != null) {
            if (requested.isBusy()) {
                throw new DuelException("error.arena-busy");
            }
            return requested;
        }

        return arenas.stream()
                .filter(a -> !a.isBusy())
                .findAny()
                .orElseThrow(() -> new DuelException("error.no-arenas-available"));
    }
}