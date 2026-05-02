package org.antix.duelsparty.command;

import org.antix.duelsparty.duel.Duel;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.entity.Player;

public class DuelAcceptCommand extends BaseCommand {
    private final DuelManager duelManager;

    public DuelAcceptCommand(DuelManager duelManager, MessageService messageService) {
        super(messageService);
        this.duelManager = duelManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        // 1. Akceptujemy i pobieramy obiekt pojedynku
        Duel duel = duelManager.acceptInvite(player);

        // 2. Odpalamy walkę!
        duel.start();

        // 3. Opcjonalnie: Komunikat dla serwera
        String lang = player.getLocale().split("_")[0];
        player.sendMessage(messageService.getMessage(lang, "success.duel-started"));
    }
}