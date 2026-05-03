package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class KitAdminCommand extends BaseCommand {
    public KitAdminCommand(MessageService messageService) {
        super(messageService);
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        if (!player.hasPermission("duelsparty.admin")) return;

        if (args.length < 2 || !args[0].equalsIgnoreCase("save")) {
            player.sendMessage("§cUżycie: /ka save <nazwa>");
            return;
        }

        String kitName = args[1].toLowerCase();
        File file = new File(DuelsPartyPlugin.getInstance().getDataFolder() + "/kits", kitName + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Pobieramy przedmioty z zachowaniem wszystkich komponentów 1.21.1
        config.set("name", kitName);
        config.set("content", Arrays.asList(player.getInventory().getStorageContents()));
        config.set("armor", Arrays.asList(player.getInventory().getArmorContents()));
        config.set("offhand", player.getInventory().getItemInOffHand());

        try {
            config.save(file);
            player.sendMessage("§aZapisano kit: " + kitName + " (Obsługa Data Components 1.21.1)");
            DuelsPartyPlugin.getInstance().getKitManager().loadKits(); // Przeładuj RAM
        } catch (IOException e) {
            player.sendMessage("§cBłąd zapisu pliku!");
            e.printStackTrace();
        }
    }
}