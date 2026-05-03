package org.antix.duelsparty.database;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NullDatabase implements DatabaseService {
    @Override
    public void saveMatchResult(UUID winner, UUID loser, int winnerKills, int loserKills) {}

    @Override
    public CompletableFuture<UserStats> loadStats(UUID uuid) {
        return CompletableFuture.completedFuture(new UserStats(uuid, 0, 0, 0, 0, 0.0));
    }

    @Override
    public void close() {}
}