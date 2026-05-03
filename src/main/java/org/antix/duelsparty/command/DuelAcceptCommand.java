package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.Duel;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.duel.InviteContext;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class DuelAcceptCommand extends BaseCommand {
    private final DuelManager duelManager;

    public DuelAcceptCommand(DuelManager duelManager, MessageService messageService) {
        super(messageService);
        this.duelManager = duelManager;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        DuelsPartyPlugin.debug("Gracz " + player.getName() + " wpisał /accept");

        // Pobieramy zaproszenie dla tego gracza
        Optional<InviteContext> inviteOpt = duelManager.getInvite(player.getUniqueId());

        if (inviteOpt.isEmpty()) {
            DuelsPartyPlugin.debug("Brak oczekujących zaproszeń dla: " + player.getName());
            player.sendMessage(messageService.getMessage(lang, "error.no-pending-invite"));
            return;
        }

        InviteContext invite = inviteOpt.get();

        // Zmieniono z challengerId() na senderUuid(), aby pasowało do rekordu w DuelManager
        Player challenger = Bukkit.getPlayer(invite.senderUuid());

        if (challenger == null || !challenger.isOnline()) {
            DuelsPartyPlugin.debug("Wzywający jest offline.");
            player.sendMessage(messageService.getMessage(lang, "error.player-offline"));
            duelManager.removeInvite(player.getUniqueId());
            return;
        }

        // Wszystko gra! Tworzymy pojedynek
        DuelsPartyPlugin.debug("Zaproszenie poprawne. Przekazuję do DuelManager...");

        List<Player> teamA = List.of(challenger);
        List<Player> teamB = List.of(player);

        try {
            // Wyciągamy kitId z zaproszenia, które teraz posiada to pole
            String kitId = invite.kitId();

            // Wywołujemy createDuel z 4 argumentami (teamA, teamB, arena, kitId)
            Duel duel = duelManager.createDuel(teamA, teamB, invite.requestedArena(), kitId);
            duel.start();

            // Czyścimy zaproszenie po sukcesie
            duelManager.removeInvite(player.getUniqueId());

        } catch (DuelException e) {
            DuelsPartyPlugin.debug("Błąd podczas tworzenia pojedynku: " + e.getMessageKey());
            player.sendMessage(messageService.getMessage(lang, e.getMessageKey()));
        }

        try {
            // Pobieramy dane z zaproszenia
            String kitId = invite.kitId();

            // Wywołujemy poprawną metodę: (List<Player>, List<Player>, Arena, String)
            Duel duel = duelManager.createDuel(teamA, teamB, invite.requestedArena(), kitId);
            duel.start();

            duelManager.removeInvite(player.getUniqueId());
        } catch (DuelException e) {
            player.sendMessage(messageService.getMessage(lang, e.getMessageKey()));
        }
    }
}