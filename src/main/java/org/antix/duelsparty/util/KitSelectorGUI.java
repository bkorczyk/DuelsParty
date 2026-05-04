package org.antix.duelsparty.util;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class KitSelectorGUI {

    public static void open(Player sender, Player target) {
        Collection<Kit> kits = DuelsPartyPlugin.getInstance().getKitManager().getKits(); // Musisz dodać getKits() do KitManager
        String lang = sender.getLocale().split("_")[0].toLowerCase();
        DuelsPartyPlugin plugin = JavaPlugin.getPlugin(DuelsPartyPlugin.class);

        String title = plugin.getMessageService().getMessage(lang, "gui.kit-title");

        // Tworzymy inventory (rozmiar musi być wielokrotnością 9)
        int size = ((kits.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(new InviteKitHolder(target), size, title);

        for (Kit kit : kits) {
            // Jako ikonę bierzemy np. pierwszy przedmiot z kitu lub miecz
            ItemStack icon = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§6§lKit: §e" + kit.displayName());
            // Ukrywamy nick celu w Lore, żeby wiedzieć kogo wyzywamy po kliknięciu
            meta.setLore(java.util.List.of("§7Kliknij, aby wyzwać gracza", "§0" + target.getName()));
            icon.setItemMeta(meta);

            inv.addItem(icon);
        }

        sender.openInventory(inv);
    }
}