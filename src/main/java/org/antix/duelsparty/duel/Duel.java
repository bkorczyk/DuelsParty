package org.antix.duelsparty.duel;

import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.duel.match.MatchState;

public class Duel {
    private MatchState state = MatchState.STARTING;
    private Arena arena;

    public Duel(Arena arena){
        this.arena = arena;
    }
    public Arena getArena(){
        return arena;
    }

    public MatchState getState() {
        return state;
    }
}