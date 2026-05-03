package org.antix.duelsparty.party;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Zmieniono z record na final class dla pełnej kontroli nad mutowalnością listy.
 */
public final class Party {
    private final String name;
    private final UUID leader;
    private final List<UUID> members;

    public Party(String name, UUID leader, List<UUID> initialMembers) {
        this.name = name;
        this.leader = leader;
        // CopyOnWriteArrayList jest idealny dla częstych odczytów i rzadkich zmian
        this.members = new CopyOnWriteArrayList<>(initialMembers);
    }

    public String getName() {return name;}
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members; }

    public boolean isLeader(UUID uuid) { return leader.equals(uuid); }
    public void addMember(UUID uuid) { if (!members.contains(uuid)) members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public int getSize() { return members.size(); }
}