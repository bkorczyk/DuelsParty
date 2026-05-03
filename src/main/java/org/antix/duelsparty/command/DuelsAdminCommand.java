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
import java.util.UUID;

public class DuelsAdminCommand implements CommandExecutor {
    private final Map<String, SubCommand> adminSubCommands = new HashMap<>();
    private final MessageService messageService;

    public DuelsAdminCommand(DuelManager duelManager, MessageService messageService) {
        this.messageService = messageService;

        // Rejestracja modułów administracyjnych
        adminSubCommands.put("arena", new SubAdminArena(duelManager, messageService));
        adminSubCommands.put("kit", new SubAdminKit(messageService));
        adminSubCommands.put("reload", (player, args, lang) -> {
            // Logika przeładowania konfiguracji
            player.sendMessage("§6§l» §aPlugin został przeładowany.");
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("duelsparty.admin")) return true; //

        String lang = player.getLocale().split("_")[0].toLowerCase();

        if (args.length < 1) {
            player.sendMessage("§6§l» §eUżycie: §f/da <arena|kit|reload>");
            return true;
        }

        SubCommand target = adminSubCommands.get(args[0].toLowerCase());
        if (target != null) {
            target.execute(player, args, lang);
        } else {
            player.sendMessage("§6§l» §cNieznana sub-komenda administracyjna.");
        }
        return true;
    }
    public void clearAllPendingSessions(UUID uuid) {
        // Szukamy sub-komendy odpowiedzialnej za areny i czyścimy jej RAM
        SubCommand arenaCmd = adminSubCommands.get("arena");
        if (arenaCmd instanceof SubAdminArena subArena) {
            subArena.clearSession(uuid);
        }
    }
}