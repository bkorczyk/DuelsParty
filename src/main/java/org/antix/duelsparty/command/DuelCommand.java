package org.antix.duelsparty.command;

import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DuelCommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final MessageService messageService;
    private final SubCommand inviteCommand; // Fallback dla /duel <nick>

    public DuelCommand(DuelManager duelManager, MessageService messageService) {
        this.messageService = messageService;

        // Rejestracja sub-komend
        this.subCommands.put("accept", new SubDuelAccept(duelManager, messageService));
        this.subCommands.put("stats", new SubDuelStats(messageService));
        this.subCommands.put("deny", (player, args, lang) -> {
            duelManager.removeInvite(player.getUniqueId());
            player.sendMessage("§6§l» §cOdrzucono zaproszenie.");
        });

        // Inicjalizacja zaproszenia jako domyślnej akcji
        this.inviteCommand = new SubDuelInvite(duelManager, messageService);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        String lang = player.getLocale().split("_")[0].toLowerCase();

        if (args.length == 0) {
            player.sendMessage(messageService.getMessage(lang, "error.usage-duel"));
            return true;
        }

        String firstArg = args[0].toLowerCase();
        SubCommand target = subCommands.get(firstArg);

        if (target != null) {
            target.execute(player, args, lang);
        } else {
            // Jeśli pierwszy argument to nie sub-komenda, traktujemy go jako nick gracza
            inviteCommand.execute(player, args, lang);
        }
        return true;
    }
}