package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.database.DatabaseService;
import org.antix.duelsparty.database.UserStats;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Sub-komenda wyświetlająca statystyki gracza pobrane z MySQL.
 * Obsługuje asynchroniczne ładowanie danych, by nie blokować Main Thread.
 */
public class SubDuelStats implements SubCommand {

    private final MessageService messageService;

    public SubDuelStats(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        DatabaseService db = DuelsPartyPlugin.getInstance().getDatabaseService();

        // Informujemy gracza, że dane są pobierane (dobre dla UX)
        player.sendMessage(messageService.getMessage(lang, "info.stats-loading"));

        // Pobieramy dane asynchronicznie
        db.loadStats(player.getUniqueId()).thenAccept(stats -> {
            // Powrót do wątku głównego nie jest tu konieczny dla wysłania wiadomości,
            // ale jeśli chcielibyśmy otworzyć GUI, musielibyśmy użyć BukkitScheduler.
            sendStatsMessage(player, stats, lang);
        }).exceptionally(ex -> {
            player.sendMessage(messageService.getMessage(lang, "error.database-fail"));
            ex.printStackTrace();
            return null;
        });
    }

    private void sendStatsMessage(Player player, UserStats stats, String lang) {
        player.sendMessage(" ");
        player.sendMessage(messageService.getMessage(lang, "stats.header"));
        player.sendMessage(messageService.getMessage(lang, "stats.wins").replace("{val}", String.valueOf(stats.wins())));
        player.sendMessage(messageService.getMessage(lang, "stats.losses").replace("{val}", String.valueOf(stats.losses())));
        player.sendMessage(messageService.getMessage(lang, "stats.eliminations").replace("{val}", String.valueOf(stats.kills())));
        player.sendMessage(messageService.getMessage(lang, "stats.falls").replace("{val}", String.valueOf(stats.deaths())));
        player.sendMessage(messageService.getMessage(lang, "stats.accuracy").replace("{val}", String.format("%.2f%%", stats.accuracy())));
        player.sendMessage(" ");
    }
}