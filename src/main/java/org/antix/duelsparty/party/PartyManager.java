package org.antix.duelsparty.party;

import org.bukkit.entity.Player;
import java.util.*;

public class PartyManager {
    private final Map<UUID, Party> playerPartyMap = new HashMap<>();

    public Party createParty(Player leader) {
        Party party = new Party(leader.getUniqueId(), new ArrayList<>(Collections.singletonList(leader.getUniqueId())));
        playerPartyMap.put(leader.getUniqueId(), party);
        return party;
    }

    public Optional<Party> getParty(Player player) {
        return Optional.ofNullable(playerPartyMap.get(player.getUniqueId()));
    }

    public void joinParty(Player player, Party party) {
        party.addMember(player.getUniqueId());
        playerPartyMap.put(player.getUniqueId(), party);
    }

    public void leaveParty(Player player) {
        Party party = playerPartyMap.remove(player.getUniqueId());
        if (party != null) {
            party.removeMember(player.getUniqueId());
            // Jeśli lider wychodzi, rozwiązujemy grupę (uproszczenie)
            if (party.isLeader(player.getUniqueId())) {
                party.members().forEach(playerPartyMap::remove);
            }
        }
    }
}