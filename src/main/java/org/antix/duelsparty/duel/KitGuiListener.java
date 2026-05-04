package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.util.InviteKitHolder;
import org.antix.duelsparty.util.QueueKitHolder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class KitGuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        Player player = (Player) event.getWhoClicked();

        // 1. Obsługa KOLEJKI PUBLICZNEJ
        if (holder instanceof QueueKitHolder) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            // Wyciągamy ID kitu (czyszcząc kolory dla pewności)
            String kitId = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())
                    .replace("Tryb: ", "").replace("Tryb: ", "") // Na wypadek różnych tłumaczeń
                    .toLowerCase();

            player.closeInventory();
            DuelsPartyPlugin.getInstance().getDuelManager().toggleQueue(player, kitId);
            return;
        }

        // 2. Obsługa ZAPROSZEŃ PRYWATNYCH
        if (holder instanceof InviteKitHolder inviteHolder) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            Player target = inviteHolder.getTarget(); // Pobieramy bezpośrednio z holdera!
            String kitName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName())
                    .replace("Kit: ", "").toLowerCase();

            if (target != null && target.isOnline()) {
                player.closeInventory();
                DuelsPartyPlugin.getInstance().getDuelManager().sendInvite(player, target, null, kitName);

                // Tutaj możesz użyć MessageService dla komunikatu wysyłania
                player.sendMessage("§6§l» §aWysłano wyzwanie na kit §f" + kitName + " §ado §f" + target.getName());
            } else {
                player.sendMessage("§c§l» §cGracz nie jest już dostępny.");
                player.closeInventory();
            }
        }
    }
}