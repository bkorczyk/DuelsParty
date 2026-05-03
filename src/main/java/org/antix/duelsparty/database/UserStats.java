package org.antix.duelsparty.database;

import java.util.UUID;

public record UserStats(
        UUID uuid,
        int wins,
        int losses,
        int kills,
        int deaths,
        double accuracy
) {}