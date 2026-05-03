package org.antix.duelsparty.command;

import org.antix.duelsparty.util.MessageService;
import org.bukkit.entity.Player;

public class DuelStatsCommand extends BaseCommand {

    public DuelStatsCommand(MessageService messageService) {
        super(messageService);
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        // Docelowo: Pobieranie z DatabaseManager asynchronicznie
        player.sendMessage("§6§l» §eTwoje statystyki (Sesja):");
        player.sendMessage("§7Zabójstwa: §f0");
        player.sendMessage("§7Śmierci: §f0");
        player.sendMessage("§7Celność: §f0%");
        player.sendMessage("§8(Pełne statystyki MySQL zostaną wdrożone w następnym module)");
    }
}