package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelInviteCommand extends BaseCommand {
    private final DuelManager duelManager;

    public DuelInviteCommand(DuelManager duelManager, MessageService messageService) {
        super(messageService);
        this.duelManager = duelManager;
    }

    @Override
    public void execute(Player player, String[] args, String lang){
        // Pobieramy język gracza (np. "pl_pl" -> "pl")

        if (args.length < 1) {
            player.sendMessage(messageService.getMessage(lang, "error.usage-duel"));
            return;
        }

        if (args.length < 1) {
            throw new DuelException("error.usage-duel");
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            throw new DuelException("error.player-offline");
        }

        // Obsługa opcjonalnej areny (drugi argument po nicku)
        org.antix.duelsparty.duel.arena.Arena requestedArena = null;
        if (args.length >= 2) {
            String arenaName = args[1];
            requestedArena = duelManager.getArenaByName(arenaName)
                    .orElseThrow(() -> new DuelException("error.arena-not-found"));
        }

// ... po walidacji targeta i areny ...

        String kitId = "default"; // Domyślnie
        if (args.length >= 3) { // Jeśli gracz wpisał /duel <nick> <arena> <kit>
            kitId = args[2];
        }

// Wywołujemy zaktualizowaną metodę sendInvite
        duelManager.sendInvite(player, target, requestedArena, kitId);

        // Wiadomości do graczy

        player.sendMessage(messageService.getMessage(lang, "success.invite-sent"));

        String targetLang = target.getLocale().split("_")[0];
        target.sendMessage(messageService.getMessage(targetLang, "info.invite-received")
                .replace("{player}", player.getName()));
    }
}