package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.command.DuelAdminCommand;
import org.antix.duelsparty.duel.match.MatchState;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


import java.util.Random;
import java.util.UUID;

public class DuelListener implements Listener {

    private final DuelManager duelManager;
    private final MessageService messageService;
    private final Random random = new Random();

    public DuelListener(DuelManager duelManager, MessageService messageService) {
        this.duelManager = duelManager;
        this.messageService = messageService;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        duelManager.getDuelByPlayer(victim).ifPresent(duel -> {
            event.getDrops().clear();
            event.setDeathMessage(null);

            Player killer = victim.getKiller();

            // 1. Wysyłanie spersonalizowanych komunikatów o śmierci
            broadcastDeathMessage(duel, victim, killer);

            // 2. Efekt wizualny (piorun w miejscu śmierci - nie zadaje obrażeń)
            victim.getWorld().strikeLightningEffect(victim.getLocation());

            // 3. Natychmiastowy respawn i zakończenie walki
            victim.spigot().respawn();

            // W systemie 1v1 zwycięzcą jest ten, kto nie zginął.
            // W systemie Party/2v2 będziesz musiał sprawdzić czy cała lista teamu padła.
            Player winner = killer != null ? killer : (duel.getAllParticipants().get(0).equals(victim)
                    ? duel.getAllParticipants().get(1) : duel.getAllParticipants().get(0));

            duel.end(winner);
            duelManager.removeDuel(duel);
        });
    }

    private void broadcastDeathMessage(Duel duel, Player victim, Player killer) {
        // Zamiast stałego 3, szukamy aż do skutku lub do limitu (np. 20)
        // W profesjonalnym systemie można by zcache'ować tę liczbę przy starcie.
        int randomId = random.nextInt(20) + 1;
        String messageKey = (killer != null) ? "duel.death." + randomId : "duel.death.broadcast-no-killer";

        duel.getAllParticipants().forEach(participant -> {
            String lang = participant.getLocale().split("_")[0].toLowerCase();
            String rawMessage = messageService.getMessage(lang, messageKey);

            // Zabezpieczenie przed brakiem klucza w configu (np. jeśli dodasz 50 wiadomości)
            if (rawMessage.contains("Missing key") && killer != null) {
                rawMessage = messageService.getMessage(lang, "duel.death.1");
            }

            String finalMessage = rawMessage
                    .replace("{victim}", victim.getName())
                    .replace("{killer}", (killer != null ? killer.getName() : "Unknown"));

            participant.sendMessage(finalMessage);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player leaver = event.getPlayer();

        duelManager.getDuelByPlayer(leaver).ifPresent(duel -> {
            // Powiadomienie pozostałych uczestników o walkowerze
            duel.getAllParticipants().stream()
                    .filter(p -> !p.equals(leaver))
                    .forEach(p -> {
                        String lang = p.getLocale().split("_")[0].toLowerCase();
                        p.sendMessage(messageService.getMessage(lang, "error.player-quit-win"));
                    });

            duelManager.removeDuel(duel);
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        duelManager.getDuelByPlayer(player).ifPresent(duel -> {
            if (duel.getState() == MatchState.STARTING) {
                Location from = event.getFrom();
                Location to = event.getTo();

                // Blokada chodzenia, pozwalamy na obracanie kamerą (pitch/yaw)
                if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                    event.setTo(from.setDirection(to.getDirection()));
                }
            }
        });
    }

    // Wewnątrz DuelListener.java - dodaj obsługę czyszczenia sesji admina
    @EventHandler
    public void onAdminQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        DuelsPartyPlugin plugin = DuelsPartyPlugin.getInstance();

        // Pobieramy główny dispatcher administracyjny
        if (plugin.getCommand("dueladmin").getExecutor() instanceof DuelAdminCommand adminCmd) {
            // Wywołujemy metodę czyszczącą w dispatcherze
            adminCmd.clearAllPendingSessions(uuid);

            if (DuelsPartyPlugin.getInstance().getConfig().getBoolean("debug")) {
                DuelsPartyPlugin.debug("Wyczyszczono sesje administracyjne dla: " + event.getPlayer().getName());
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. Obsługa Party - usunięcie gracza z grupy przy wyjściu (Safety First)
        DuelsPartyPlugin.getInstance().getPartyManager().leaveParty(player);

        // 2. Obsługa sesji admina - POPRAWIONY IF
        org.bukkit.command.PluginCommand cmd = DuelsPartyPlugin.getInstance().getCommand("dueladmin");
        if (cmd != null && cmd.getExecutor() instanceof DuelAdminCommand adminCmd) {
            adminCmd.clearAllPendingSessions(uuid);
            DuelsPartyPlugin.debug("Wyczyszczono sesje administracyjne dla: " + player.getName());
        }
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) return;

        // Sprawdzenie Friendly Fire w Party[cite: 1]
        var partyManager = DuelsPartyPlugin.getInstance().getPartyManager();
        var attackerParty = partyManager.getParty(attacker);
        var victimParty = partyManager.getParty(victim);

        if (attackerParty.isPresent() && victimParty.isPresent()) {
            if (attackerParty.get().equals(victimParty.get())) {
                // Jeśli są w tym samym party, sprawdzamy czy trwa walka FFA[cite: 1]
                var duel = DuelsPartyPlugin.getInstance().getDuelManager().getDuelByPlayer(attacker);

                // Jeśli nie ma pojedynku LUB jest faza odliczania - blokujemy FF[cite: 1]
                if (duel.isEmpty() || duel.get().getState() == MatchState.STARTING) {
                    event.setCancelled(true);
                    return;
                }
                // Tu można dodać: if (duel.getType() != DuelType.PARTY_FFA) event.setCancelled(true);
            }
        }

        // Istniejąca logika blokady obrażeń w fazie STARTING dla graczy spoza tego samego party[cite: 1]
        DuelsPartyPlugin.getInstance().getDuelManager().getDuelByPlayer(attacker).ifPresent(duel -> {
            if (duel.getState() == MatchState.STARTING) {
                event.setCancelled(true);
            }
        });
    }
}