package org.antix.duelsparty.database;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DatabaseService {
    void saveMatchResult(UUID winner, UUID loser, int winnerKills, int loserKills);
    CompletableFuture<UserStats> loadStats(UUID uuid);
    void close();
}