package org.antix.duelsparty.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Party(UUID leader, List<UUID> members) {
    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public void addMember(UUID uuid) {
        if (!members.contains(uuid)) members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}