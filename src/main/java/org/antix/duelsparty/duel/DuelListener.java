package org.antix.duelsparty.duel;

import org.antix.duelsparty.duel.match.MatchState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {

    private final DuelManager duelManager;

    public DuelListener(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) return;

        // Szukamy, czy któryś z graczy jest w aktywnym pojedynku
        duelManager.getDuelByPlayer(attacker).ifPresent(duel -> {
            // Jeśli odliczanie jeszcze trwa, blokujemy hity
            if (duel.getState() == MatchState.STARTING) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        duelManager.getDuelByPlayer(victim).ifPresent(duel -> {
            // Tutaj logika zakończenia walki
            Player winner = duel.getPlayer1().equals(victim) ? duel.getPlayer2() : duel.getPlayer1();

            winner.sendMessage("§aZwyciężyłeś w pojedynku przeciwko §f" + victim.getName());
            victim.sendMessage("§cPrzegrałeś pojedynek.");

            // Sprzątanie po walce
            // duel.end(winner); <- To dopiszemy za chwilę
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        duelManager.getDuelByPlayer(event.getPlayer()).ifPresent(duel -> {
            // Jeśli ktoś wyjdzie z serwera, walka musi zostać przerwana
            // walkower dla drugiego gracza
        });
    }
}