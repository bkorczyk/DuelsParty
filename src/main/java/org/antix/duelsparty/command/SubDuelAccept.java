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

/**
 * Sub-komenda obsługująca akceptację zaproszenia do pojedynku.
 * Zaktualizowana pod kątem nowego rekordu InviteContext (4 argumenty).
 */
public class SubDuelAccept implements SubCommand {

    private final DuelManager duelManager;
    private final MessageService messageService;

    public SubDuelAccept(DuelManager duelManager, MessageService messageService) {
        this.duelManager = duelManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        // 1. Pobranie zaproszenia z DuelManager (zwraca Optional<InviteContext>)
        InviteContext invite = duelManager.getInvite(player.getUniqueId())
                .orElseThrow(() -> new DuelException("error.no-pending-invite"));

        // 2. Sprawdzenie terminu ważności (isExpired() zaimplementowane w rekordzie)
        if (invite.isExpired()) {
            duelManager.clearInvite(player.getUniqueId()); // Zmieniono na clearInvite dla spójności nazw
            throw new DuelException("error.invite-expired");
        }

        // 3. Walidacja wyzywającego - używamy pola senderLeader() z nowego rekordu
        Player challenger = Bukkit.getPlayer(invite.senderLeader());
        if (challenger == null || !challenger.isOnline()) {
            duelManager.clearInvite(player.getUniqueId());
            throw new DuelException("error.player-offline");
        }

        // 4. Przygotowanie drużyn (Immutable Lists dla bezpieczeństwa wątkowego)
        List<Player> teamA = List.of(challenger);
        List<Player> teamB = List.of(player);

        // 5. Inicjalizacja pojedynku - używamy pól arena() i kitId() z rekordu
        // Upewnij się, że DuelManager posiada metodę createDuel(List, List, Arena, String)
        Duel duel = duelManager.createDuel(
                teamA,
                teamB,
                invite.arena(), // Zmieniono z requestedArena() na arena()
                invite.kitId()
        );

        // 6. Start walki (Teleportacja, nadanie zestawów)
        duel.start();

        // 7. Finalizacja: Usunięcie zaproszenia z ConcurrentHashMap
        duelManager.clearInvite(player.getUniqueId());

        DuelsPartyPlugin.debug("Pojedynek 1v1 wystartował: " + challenger.getName() + " vs " + player.getName());
    }
}