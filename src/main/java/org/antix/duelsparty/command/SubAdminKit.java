package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SubAdminKit implements SubCommand {
    private final MessageService messageService;

    public SubAdminKit(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        if (args.length < 3 || !args[1].equalsIgnoreCase("save")) {
            player.sendMessage("§6§l» §cPoprawne użycie: §f/da kit save <nazwa>");
            return;
        }

        String kitName = args[2].toLowerCase();
        // Zapis asynchroniczny plików konfiguracyjnych - Złota Zasada Wydajności
        DuelsPartyPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(DuelsPartyPlugin.getInstance(), () -> {
            saveKit(player, kitName);
        });
    }

    private void saveKit(Player player, String kitName) {
        File file = new File(DuelsPartyPlugin.getInstance().getDataFolder() + "/kits", kitName + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Paper API 1.21.1+ obsługuje Data Components bezpośrednio w serializacji ItemStack
        config.set("name", kitName);
        config.set("content", Arrays.asList(player.getInventory().getStorageContents()));
        config.set("armor", Arrays.asList(player.getInventory().getArmorContents()));
        config.set("offhand", player.getInventory().getItemInOffHand());

        try {
            config.save(file);
            // Powrót na Main Thread, aby odświeżyć KitManagera w RAM
            DuelsPartyPlugin.getInstance().getServer().getScheduler().runTask(DuelsPartyPlugin.getInstance(), () -> {
                DuelsPartyPlugin.getInstance().getKitManager().loadKits();
                player.sendMessage("§6§l» §aKit §f" + kitName + " §azostał zapisany (Data Components OK).");
            });
        } catch (IOException e) {
            player.sendMessage("§6§l» §cBłąd podczas zapisu pliku kitu.");
            e.printStackTrace();
        }
    }
}