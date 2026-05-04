package org.antix.duelsparty.util;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

// InviteKitHolder.java
public class InviteKitHolder implements org.bukkit.inventory.InventoryHolder {
    private final Player target;
    public InviteKitHolder(Player target) { this.target = target; }
    public Player getTarget() { return target; }
    @Override public org.bukkit.inventory.Inventory getInventory() { return null; }
}
