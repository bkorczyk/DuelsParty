package org.antix.duelsparty.duel;

import org.antix.duelsparty.duel.arena.Arena;
import java.util.UUID;

public record InviteContext(UUID senderUuid, Arena requestedArena, long timestamp) {
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 30000; // 30 sekund na akceptację
    }
}