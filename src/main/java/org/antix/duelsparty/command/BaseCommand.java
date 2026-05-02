package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand implements CommandExecutor {
    protected final MessageService messageService;

    protected BaseCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        try {
            execute(player, args);
        } catch (DuelException e) {
            // Pobieramy locale gracza (zwraca np. "pl_pl")
            String fullLocale = player.getLocale().toLowerCase();

            // Wyciągamy pierwsze dwa znaki (np. "pl"), żeby pasowało do naszych plików
            String lang = fullLocale.split("_")[0];

            // Wysyłamy przetłumaczony błąd
            player.sendMessage(messageService.getMessage(lang, e.getMessageKey()));
        }

        return true;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            throw new DuelException("error.usage-duel");
        }

        Player target = Bukkit.getPlayer(args[0]);
        duelManager.sendInvite(player, target);

        // Wiadomość dla wysyłającego (przetłumaczona przez BaseCommand)
        player.sendMessage(messageService.getMessage(player.getLocale().split("_")[0], "success.invite-sent"));

        // Wiadomość dla zaproszonego
        target.sendMessage(messageService.getMessage(target.getLocale().split("_")[0], "info.invite-received")
                .replace("{player}", player.getName()));
    }
}