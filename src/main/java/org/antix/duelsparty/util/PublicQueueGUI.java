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
import java.util.List;

public class PublicQueueGUI {

    public static void open(Player player) {
        Collection<Kit> kits = DuelsPartyPlugin.getInstance().getKitManager().getKits();
        String lang = player.getLocale().split("_")[0].toLowerCase();
        DuelsPartyPlugin plugin = JavaPlugin.getPlugin(DuelsPartyPlugin.class);

        int size = ((kits.size() / 9) + 1) * 9;
        // Zmieniamy tytuł, aby Listener wiedział, że to menu kolejki, a nie zaproszenia
        String title = plugin.getMessageService().getMessage(lang, "gui.queue-title");
        Inventory inv = Bukkit.createInventory(new QueueKitHolder(), size, title);

        for (Kit kit : kits) {
            ItemStack icon = new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§6§lTryb: §e" + kit.displayName());
            meta.setLore(List.of(
                    "§7Kliknij, aby szukać przeciwnika",
                    "§7w trybie §f" + kit.id()
            ));
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }

        player.openInventory(inv);
    }
}