package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.duel.InviteContext;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.party.Party;
import org.antix.duelsparty.party.PartyManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubDuelParty implements SubCommand {
    private final PartyManager partyManager;
    private final MessageService messageService;
    private final DuelManager duelManager;

    public SubDuelParty(PartyManager partyManager, DuelManager duelManager, MessageService messageService) {
        this.partyManager = partyManager;
        this.duelManager = duelManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {



        if (args.length < 2) {
            player.sendMessage(messageService.getMessage(lang, "error.usage-party"));
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "create" -> handleCreate(player, args, lang);
            case "invite" -> handleInvite(player, args, lang);
            case "join" -> handleJoin(player, lang);
            case "leave" -> partyManager.leaveParty(player);
            case "challenge" -> handlePartyChallenge(player, args, lang);
            case "acceptduel" -> handleAcceptPartyDuel(player, lang);
            default -> player.sendMessage(messageService.getMessage(lang, "error.unknown-action"));
        }
    }

    private void handleInvite(Player player, String[] args, String lang) {
        if (args.length < 3) return;
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) return;

        partyManager.sendInvite(player, target);
        player.sendMessage(messageService.getMessage(lang, "success.party-invite-sent"));
        target.sendMessage(messageService.getMessage(target.getLocale().split("_")[0], "info.party-invite-received")
                .replace("{player}", player.getName()));
    }

    private void handleJoin(Player player, String lang) {
        partyManager.joinParty(player);
        player.sendMessage(messageService.getMessage(lang, "success.party-joined"));
    }

    private void handleCreate(Player player, String[] args, String lang) {
        if (args.length < 3) throw new DuelException("error.usage-party-create");

        String partyName = args[2];
        Party party = partyManager.createParty(player, partyName);

        player.sendMessage(messageService.getMessage(lang, "success.party-created")
                .replace("{name}", party.getName()));
    }
    private void handlePartyChallenge(Player player, String[] args, String lang) {
        if (args.length < 3) throw new DuelException("error.usage-party-challenge");

        Player targetLeader = Bukkit.getPlayer(args[2]);
        if (targetLeader == null) throw new DuelException("error.player-offline");

        // Domyślne wartości lub pobrane z komendy
        Arena arena = args.length >= 4 ? duelManager.getArenaByName(args[3]).orElse(null) : null;
        String kitId = args.length >= 5 ? args[4] : "default";

        DuelsPartyPlugin.getInstance().getDuelManager().sendPartyInvite(player, targetLeader, arena, kitId);

        player.sendMessage(messageService.getMessage(lang, "success.party-duel-sent")
                .replace("{target}", targetLeader.getName()));

        targetLeader.sendMessage(messageService.getMessage(targetLeader.getLocale().split("_")[0], "info.party-duel-received")
                .replace("{sender}", player.getName()));
    }

    private void handleAcceptPartyDuel(Player player, String lang) {
        DuelManager dm = DuelsPartyPlugin.getInstance().getDuelManager();
        UUID targetUuid = player.getUniqueId();

        if (!dm.hasPartyInvite(targetUuid)) throw new DuelException("error.no-party-duel-invite");

        // Pobieramy dane zaproszenia i tworzymy walkę
        UUID senderUuid = dm.getPartyInviteSender(targetUuid);
        InviteContext ctx = dm.getPartyInviteContext(targetUuid);

        Party partyA = DuelsPartyPlugin.getInstance().getPartyManager().getParty(Bukkit.getPlayer(senderUuid)).get();
        Party partyB = DuelsPartyPlugin.getInstance().getPartyManager().getParty(player).get();

        // Startujemy walkę Party vs Party!
        dm.createPartyDuel(partyA, partyB, ctx.arena(), ctx.kitId());
    }
}