package org.antix.duelsparty.duel.match;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MatchPlayerData {
    private final Location backLocation;
    private final ItemStack[] savedInventory;
    private final ItemStack[] savedArmor;
    // Statystyki z TDD do późniejszego zapisu w MySQL
    private int kills = 0;
    private int arrowsShot = 0;
    private int arrowsHit = 0;

    public MatchPlayerData(Player player) {
        this.backLocation = player.getLocation();
        this.savedInventory = player.getInventory().getContents();
        this.savedArmor = player.getInventory().getArmorContents();
    }

    public void restore(Player player) {
        player.getInventory().setContents(savedInventory);
        player.getInventory().setArmorContents(savedArmor);
        player.teleport(backLocation);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }
    // Gettery/Settery dla statystyk
}