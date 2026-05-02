package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.arena.Arena;
import org.bukkit.entity.Player;
import java.util.*;

public class DuelManager {
    private final List<Arena> arenas = new ArrayList<>();
    // Mapa: UUID zaproszonego -> Kontekst zaproszenia
    private final Map<UUID, InviteContext> pendingInvites = new HashMap<>();
    private final List<Duel> activeDuels = new ArrayList<>();

    public void addArena(Arena arena) {
        arenas.add(arena);
    }

    // Metoda pomocnicza do szukania areny po nazwie (potrzebna do komendy)
    public Optional<Arena> getArenaByName(String name) {
        return arenas.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }



    private void validatePlayers(Player p1, Player p2) {
        if (p1 == null || p2 == null || !p1.isOnline() || !p2.isOnline()) {
            throw new DuelException("error.player-offline");
        }
        if (p1.equals(p2)) {
            throw new DuelException("error.self-duel");
        }
    }

    private Arena validateRequestedArena(Arena arena) {
        if (arena.isBusy()) {
            throw new DuelException("error.arena-busy");
        }
        return arena;
    }

    private Arena findFreeArena() {
        return arenas.stream()
                .filter(a -> !a.isBusy())
                .findAny()
                .orElseThrow(() -> new DuelException("error.no-arenas-available"));
    }

    // Zaproszenia (teraz mogą przechowywać informację o wybranej arenie)
    // Na razie uprośćmy: zaproszenie rezerwuje "możliwość walki"
    public void sendInvite(Player sender, Player target, Arena arena) {
        validatePlayers(sender, target);

        // Sprawdzamy czy konkretna arena jest wolna (jeśli wybrana)
        if (arena != null && arena.isBusy()) {
            throw new DuelException("error.arena-busy");
        }

        pendingInvites.put(target.getUniqueId(), new InviteContext(
                sender.getUniqueId(),
                arena,
                System.currentTimeMillis()
        ));
    }

    public Duel acceptInvite(Player target) {
        InviteContext context = pendingInvites.remove(target.getUniqueId());

        if (context == null || context.isExpired()) {
            throw new DuelException("error.no-pending-invite");
        }

        Player sender = org.bukkit.Bukkit.getPlayer(context.senderUuid());
        if (sender == null || !sender.isOnline()) {
            throw new DuelException("error.player-offline");
        }

        // Używamy areny z kontekstu, jeśli była wybrana
        return createDuel(sender, target, context.requestedArena());
    }
    /**
     * Główna logika tworzenia pojedynku.
     * @param p1 pierwszy gracz
     * @param p2 drugi gracz
     * @param requestedArena może być null - wtedy system szuka wolnej.
     */
    public Duel createDuel(Player p1, Player p2, Arena requestedArena) {
        validatePlayers(p1, p2);

        // Logika wyboru areny: requested -> findFree -> Exception
        Arena arena = (requestedArena != null && !requestedArena.isBusy()) ?
                requestedArena : arenas.stream().filter(a -> !a.isBusy()).findAny()
                .orElseThrow(() -> new DuelException("error.no-arenas-available"));

        arena.setBusy(true);
        Duel duel = new Duel(p1, p2, arena);
        activeDuels.add(duel);
        return duel;
    }
    public Optional<Duel> getDuelByPlayer(Player player) {
        return activeDuels.stream()
                .filter(duel -> duel.getPlayer1().equals(player) || duel.getPlayer2().equals(player))
                .findFirst();
    }
}