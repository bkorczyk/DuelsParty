package org.antix.duelsparty.party;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.DuelsPartyPlugin;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {
    private final DuelsPartyPlugin plugin;
    private final Map<UUID, Party> playerPartyMap = new ConcurrentHashMap<>();
    // Nazwy zajęte w RAM
    private final Set<String> activePartyNames = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // Zaproszenia: TargetUUID -> SenderUUID
    private final Map<UUID, UUID> partyInvites = new ConcurrentHashMap<>();
    private final Map<UUID, Long> inviteTimestamp = new ConcurrentHashMap<>();

    public PartyManager(DuelsPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public Party createParty(Player leader, String name) {
        // Walidacja nazwy
        if (name.length() < 3 || name.length() > 16) throw new DuelException("error.party-name-length");
        if (activePartyNames.contains(name.toLowerCase())) throw new DuelException("error.party-name-exists");
        if (playerPartyMap.containsKey(leader.getUniqueId())) throw new DuelException("error.already-in-party");

        Party party = new Party(name, leader.getUniqueId(), new ArrayList<>(List.of(leader.getUniqueId())));

        playerPartyMap.put(leader.getUniqueId(), party);
        activePartyNames.add(name.toLowerCase());
        return party;
    }

    public void sendInvite(Player sender, Player target) {
        UUID senderUuid = sender.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        Party party = getParty(sender).orElseThrow(() -> new DuelException("error.no-party-to-invite"));

        if (!party.isLeader(senderUuid)) {
            throw new DuelException("error.not-party-leader");
        }

        partyInvites.put(targetUuid, senderUuid);
        inviteTimestamp.put(targetUuid, System.currentTimeMillis());
        DuelsPartyPlugin.debug("Zaproszenie do party: " + sender.getName() + " -> " + target.getName());
    }

    public void joinParty(Player player) {
        UUID targetUuid = player.getUniqueId();

        if (!hasValidInvite(targetUuid)) {
            throw new DuelException("error.no-party-invite");
        }

        UUID leaderUuid = partyInvites.remove(targetUuid);
        inviteTimestamp.remove(targetUuid);

        Party party = playerPartyMap.get(leaderUuid);
        if (party == null) throw new DuelException("error.party-no-longer-exists");

        party.addMember(targetUuid);
        playerPartyMap.put(targetUuid, party);
    }

    public void leaveParty(Player player) {
        Party party = playerPartyMap.remove(player.getUniqueId());
        if (party == null) return;

        party.removeMember(player.getUniqueId());

        if (party.isLeader(player.getUniqueId())) {
            activePartyNames.remove(party.getName().toLowerCase());
            party.getMembers().forEach(playerPartyMap::remove);
        }
    }

    public boolean hasValidInvite(UUID targetUuid) {
        if (!partyInvites.containsKey(targetUuid)) return false;
        // Zaproszenie wygasa po 60 sekundach
        if (System.currentTimeMillis() - inviteTimestamp.getOrDefault(targetUuid, 0L) > 60000) {
            partyInvites.remove(targetUuid);
            inviteTimestamp.remove(targetUuid);
            return false;
        }
        return true;
    }

    public Optional<Party> getParty(Player player) {
        return Optional.ofNullable(playerPartyMap.get(player.getUniqueId()));
    }
}