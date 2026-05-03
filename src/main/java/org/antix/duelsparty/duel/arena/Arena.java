package org.antix.duelsparty.duel.arena;


import org.antix.duelsparty.util.ArenaLocation;

public class Arena {
    private final String name;
    private final ArenaLocation spawn1;
    private final ArenaLocation spawn2;
    private boolean busy = false;

    public Arena(String name, ArenaLocation spawn1, ArenaLocation spawn2) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.busy = false;
    }

    // Gettery...
    public ArenaLocation spawn1() { return spawn1; }
    public ArenaLocation spawn2() { return spawn2; }
    public String getName() { return name; }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }
}