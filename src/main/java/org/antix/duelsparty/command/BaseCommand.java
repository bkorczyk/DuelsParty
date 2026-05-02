package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.util.MessageService;
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
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        try {
            execute(player, args);
        } catch (DuelException e) {
            // Logika wyciągania języka i wysyłania błędu
            String lang = player.getLocale().split("_")[0].toLowerCase();
            player.sendMessage(messageService.getMessage(lang, e.getMessageKey()));
        } catch (Exception e) {
            // Obsługa niespodziewanych błędów, żeby nie wywaliło pluginu
            player.sendMessage("§cAn internal error occurred.");
            e.printStackTrace();
        }

        return true;
    }

    // To jest kluczowe: metoda jest abstrakcyjna i nie ma ciała!
    // Każda komenda (Invite, Accept) wypełni ją po swojemu.
    public abstract void execute(Player player, String[] args);
}