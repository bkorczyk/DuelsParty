package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.util.KitSelectorGUI;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SubDuelInvite implements SubCommand {
    private final DuelManager duelManager;
    private final MessageService messageService;

    public SubDuelInvite(DuelManager duelManager, MessageService messageService) {
        this.duelManager = duelManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            throw new DuelException("error.player-offline");
        }

        if (target.equals(player)) {
            throw new DuelException("error.self-duel");
        }

        // --- LOGIKA HYBRYDOWA ---

        // 1. Jeśli gracz wpisał tylko /duel <nick> -> Otwieramy GUI
        if (args.length == 1) {
            KitSelectorGUI.open(player, target);
            return;
        }

        // 2. Jeśli gracz wpisał więcej (np. /duel nick arena kit) -> Obsługujemy tekstowo (stary kod)

        // Arena (args[1])
        Arena requestedArena = null;
        if (args.length >= 2) {
            requestedArena = duelManager.getArenaByName(args[1])
                    .orElseThrow(() -> new DuelException("error.arena-not-found"));
        }

        // Kit (args[2])
        String kitId = (args.length >= 3) ? args[2] : "default";

        duelManager.sendInvite(player, target, requestedArena, kitId);

        // Komunikacja (stara, dobra wiadomość)
        player.sendMessage(messageService.getMessage(lang, "success.invite-sent"));
        String targetLang = target.getLocale().split("_")[0].toLowerCase();
        target.sendMessage(messageService.getMessage(targetLang, "info.invite-received")
                .replace("{player}", player.getName()));
    }
}