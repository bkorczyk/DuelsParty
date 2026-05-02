package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelInviteCommand extends BaseCommand {
    private final DuelManager duelManager;

    public DuelInviteCommand(DuelManager duelManager, MessageService messageService) {
        super(messageService);
        this.duelManager = duelManager;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            throw new DuelException("error.usage-duel");
        }

        Player target = Bukkit.getPlayer(args[0]);
        duelManager.sendInvite(player, target);

        // Sukces wysłania (język brany z locale gracza)
        String lang = player.getLocale().split("_")[0];
        player.sendMessage(messageService.getMessage(lang, "success.invite-sent"));

        // Powiadomienie dla celu
        if (target != null) {
            String targetLang = target.getLocale().split("_")[0];
            target.sendMessage(messageService.getMessage(targetLang, "info.invite-received")
                    .replace("{player}", player.getName()));
        }
    }
}