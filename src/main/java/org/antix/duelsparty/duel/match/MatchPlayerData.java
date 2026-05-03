package org.antix.duelsparty.duel.match;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MatchPlayerData {
    private final Location backLocation;
    private final ItemStack[] savedInventory;
    private final ItemStack[] savedArmor;

    // Statystyki meczowe
    private int kills = 0;
    private int deaths = 0;
    private int arrowsShot = 0;
    private int arrowsHit = 0;

    public MatchPlayerData(@NotNull Player player) {
        this.backLocation = player.getLocation();
        // Klonujemy zawartość, aby uniknąć referencji do aktualnego eq gracza
        this.savedInventory = player.getInventory().getContents().clone();
        this.savedArmor = player.getInventory().getArmorContents().clone();
    }

    /**
     * Przywraca stan gracza sprzed pojedynku.
     * Wykonywane na Main Thread.
     */
    public void restore(@NotNull Player player) {
        player.getInventory().setContents(savedInventory);
        player.getInventory().setArmorContents(savedArmor);
        player.teleport(backLocation);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    // --- Logika statystyk ---

    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }
    public void addArrowShot() { this.arrowsShot++; }
    public void addArrowHit() { this.arrowsHit++; }

    public double getAccuracy() {
        if (arrowsShot == 0) return 0.0;
        return ((double) arrowsHit / arrowsShot) * 100.0;
    }

    // --- Gettery ---

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getArrowsShot() { return arrowsShot; }
    public int getArrowsHit() { return arrowsHit; }
}