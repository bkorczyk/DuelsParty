package org.antix.duelsparty.command;

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
        // Tu wywołujemy logikę akceptacji, którą wcześniej dodaliśmy do Managera
        duelManager.acceptInvite(player);

        // Pobieramy język gracza do komunikatu o sukcesie
        String lang = player.getLocale().split("_")[0];
        player.sendMessage(messageService.getMessage(lang, "success.duel-started"));
    }
}