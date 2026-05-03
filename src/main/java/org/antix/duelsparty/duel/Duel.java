package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.database.DatabaseService;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.duel.kit.Kit;
import org.antix.duelsparty.duel.match.MatchPlayerData;
import org.antix.duelsparty.duel.match.MatchState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class Duel {
    private final Arena arena;
    private final Kit kit; // Nowe pole
    private MatchState state = MatchState.PREPARATION;
    private final List<UUID> teamA = new ArrayList<>();
    private final List<UUID> teamB = new ArrayList<>();
    private final Map<UUID, MatchPlayerData> matchData = new HashMap<>();

    public Duel(List<Player> teamA, List<Player> teamB, Arena arena, Kit kit) {
        this.arena = arena;
        this.kit = kit; // Przypisujemy wstrzyknięty zestaw
        teamA.forEach(p -> this.teamA.add(p.getUniqueId()));
        teamB.forEach(p -> this.teamB.add(p.getUniqueId()));
    }

    public void start() {
        DuelsPartyPlugin.debug("Starting duel with kit: " + kit.id());
        this.state = MatchState.STARTING;

        getAllParticipants().forEach(p -> {
            matchData.put(p.getUniqueId(), new MatchPlayerData(p));

            // 1. Teleportacja
            Location spawn = teamA.contains(p.getUniqueId()) ?
                    arena.spawn1().toLocation() : arena.spawn2().toLocation();
            p.teleport(spawn);

            // 2. Nadanie kitu (Dostęp przez Main instance tylko dla managera)
            DuelsPartyPlugin.getInstance().getKitManager().applyKit(p, kit.id());
        });

        runCountdown();
    }

    private void runCountdown() {
        // Pobieramy instancję pluginu, aby odpalić zadanie
        DuelsPartyPlugin plugin = JavaPlugin.getPlugin(DuelsPartyPlugin.class);

        new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (state == MatchState.ENDING) { // Przerwij, jeśli ktoś wyszedł w trakcie
                    this.cancel();
                    return;
                }

                if (seconds > 0) {
                    getAllParticipants().forEach(p -> {
                        p.sendMessage("§6§l» §eStart za §f" + seconds + "§e...");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    });
                    seconds--;
                } else {
                    state = MatchState.FIGHTING;
                    getAllParticipants().forEach(p -> {
                        p.sendMessage("§6§l» §a§lWALKA!");
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    });
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 ticków = 1 sekunda
    }

    public void end(Player winner) {
        this.state = MatchState.ENDING;

        // 1. Wyznaczenie przegranego (dla systemu 1v1)
        Player loser = null;
        if (winner != null) {
            loser = getAllParticipants().stream()
                    .filter(p -> !p.equals(winner))
                    .findFirst().orElse(null);
        }

        // 2. Pobranie instancji bazy danych
        DatabaseService db = DuelsPartyPlugin.getInstance().getDatabaseService();

        // 3. Asynchroniczny zapis wyniku, jeśli mamy zwycięzcę
        if (winner != null && loser != null && db != null) {
            MatchPlayerData winnerData = matchData.get(winner.getUniqueId());
            MatchPlayerData loserData = matchData.get(loser.getUniqueId());

            // Przekazujemy UUID oraz zabójstwa zebrane podczas walki
            db.saveMatchResult(
                    winner.getUniqueId(),
                    loser.getUniqueId(),
                    winnerData.getKills(),
                    loserData.getKills()
            );
        }

        getAllParticipants().forEach(p -> {
            String lang = p.getLocale().split("_")[0].toLowerCase();

            // 1. Ogłoszenie zwycięzcy
            if (winner != null) {
                String winMsg = DuelsPartyPlugin.getInstance().getMessageService()
                        .getMessage(lang, "duel.end.winner")
                        .replace("{winner}", winner.getName());
                p.sendMessage(winMsg);
                // Opcjonalnie Title:
                p.sendTitle("§a§lGoniec", "§f" + winner.getName() + " wygrywa!", 10, 70, 20);
            }

            // 2. Przywracanie stanu gracza (HP, EQ, Pozycja sprzed walki)
            MatchPlayerData data = matchData.get(p.getUniqueId());
            if (data != null) {
                data.restore(p); // To powinno go też przeteleportować tam skąd przyszedł
            }
        });

        arena.setBusy(false);
        DuelsPartyPlugin.debug("Duel ended. Arena " + arena.getName() + " is now free.");
    }


    public List<Player> getAllParticipants() {
        List<UUID> allUuids = new ArrayList<>(teamA);
        allUuids.addAll(teamB);
        return allUuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public MatchState getState() { return state; }
    public Arena getArena() { return arena; }
    public boolean containsPlayer(Player player) {
        return teamA.contains(player.getUniqueId()) || teamB.contains(player.getUniqueId());
    }
    public Player getPlayer1() { return teamA.isEmpty() ? null : Bukkit.getPlayer(teamA.get(0)); }
    public Player getPlayer2() { return teamB.isEmpty() ? null : Bukkit.getPlayer(teamB.get(0)); }
}