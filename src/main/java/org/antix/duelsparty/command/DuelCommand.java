package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Główny kontroler komendy /duel.
 * Implementuje wzorzec Command Dispatcher dla zachowania zasad SOLID.
 */
public class DuelCommand implements CommandExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final MessageService messageService;
    private final SubCommand inviteCommand; // Domyślna akcja: /duel <nick>

    public DuelCommand(DuelManager duelManager, MessageService messageService) {
        this.messageService = messageService;

        // 1. Rejestracja standardowych akcji 1v1
        this.subCommands.put("accept", new SubDuelAccept(duelManager, messageService));
        this.subCommands.put("stats", new SubDuelStats(messageService));
        this.subCommands.put("deny", (player, args, lang) -> {
            duelManager.removeInvite(player.getUniqueId());
            player.sendMessage(messageService.getMessage(lang, "success.invite-denied"));
        });

        // 2. Rejestracja modułu Party
        // Przekazujemy instancje managerów pobrane z Main
        this.subCommands.put("party", new SubDuelParty(
                DuelsPartyPlugin.getInstance().getPartyManager(),
                duelManager,
                messageService
        ));

        // 3. Inicjalizacja fallbacku (wyzwania 1v1)
        this.inviteCommand = new SubDuelInvite(duelManager, messageService);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTa komenda jest dostępna tylko dla graczy.");
            return true;
        }

        // Pobieramy język gracza dla komunikatów
        String lang = player.getLocale().split("_")[0].toLowerCase();

        // Podstawowa walidacja użycia
        if (args.length == 0) {
            player.sendMessage(messageService.getMessage(lang, "error.usage-duel"));
            return true;
        }

        try {
            String firstArg = args[0].toLowerCase();
            SubCommand target = subCommands.get(firstArg);

            if (target != null) {
                // Wykonujemy dedykowaną sub-komendę (np. party, accept, stats)
                target.execute(player, args, lang);
            } else {
                // Jeśli pierwszy argument to nie sub-komenda, traktujemy go jako nick
                inviteCommand.execute(player, args, lang);
            }
        } catch (DuelException e) {
            // Przechwytywanie błędów biznesowych i wysyłanie ich do gracza[cite: 1]
            player.sendMessage(messageService.getMessage(lang, e.getMessageKey()));
        } catch (Exception e) {
            // Logowanie błędów krytycznych (nieprzewidzianych)
            player.sendMessage("§cWystąpił błąd wewnętrzny. Zgłoś to administracji.");
            e.printStackTrace();
        }

        return true;
    }
}