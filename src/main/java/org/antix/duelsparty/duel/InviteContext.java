package org.antix.duelsparty.duel;

import org.antix.duelsparty.duel.arena.Arena;
import java.util.UUID;

/**
 * Kontekst zaproszenia do walki.
 * Kolejność pól musi ściśle odpowiadać wywołaniu w konstruktorze.
 */
public record InviteContext(
        UUID senderLeader, // Kto wyzywa
        Arena arena,       // Na jakiej arenie (może być null dla losowej)
        String kitId,      // Jaki zestaw
        long timestamp     // Kiedy wysłano (do walidacji TTL)
) {
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 60000; // 60s ważności
    }
}