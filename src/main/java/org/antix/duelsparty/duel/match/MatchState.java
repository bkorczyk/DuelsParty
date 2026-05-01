package org.antix.duelsparty.duel.match;

public enum MatchState {
    STARTING,  // Odliczanie (3, 2, 1...)
    FIGHTING,  // Walka trwa
    ENDING     // Walka zakończona, teleportacja na spawn
}