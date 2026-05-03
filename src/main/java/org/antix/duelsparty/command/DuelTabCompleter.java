package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DuelTabCompleter implements TabCompleter {

    private static final List<String> SUB_COMMANDS = Arrays.asList("accept", "deny", "stats", "party");
    private static final List<String> PARTY_ACTIONS = Arrays.asList("create", "invite", "join", "leave", "challenge", "acceptduel");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String current = args[args.length - 1].toLowerCase();

        // Poziom 1: /duel <nick|subcommand>
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(SUB_COMMANDS);
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.getName().equalsIgnoreCase(sender.getName())) suggestions.add(p.getName());
            });
            return filter(suggestions, current);
        }

        // Obsługa specyficzna dla sub-komend
        String firstArg = args[0].toLowerCase();

        if (firstArg.equals("party")) {
            // Poziom 2: /duel party <action>
            if (args.length == 2) {
                return filter(PARTY_ACTIONS, current);
            }
            // Poziom 3: /duel party invite/challenge <gracz>
            if (args.length == 3) {
                String action = args[1].toLowerCase();
                if (action.equals("invite") || action.equals("challenge")) {
                    return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), current);
                }
            }
            return new ArrayList<>();
        }

        // Obsługa standardowego wyzwania: /duel <nick> <arena> <kit>
        // Jeśli pierwszy argument to NIE jest sub-komenda, to musi to być Nick gracza
        if (!SUB_COMMANDS.contains(firstArg)) {
            if (args.length == 2) {
                return filter(DuelsPartyPlugin.getInstance().getDuelManager().getArenaNames(), current);
            }
            if (args.length == 3) {
                return filter(new ArrayList<>(DuelsPartyPlugin.getInstance().getKitManager().getKitNames()), current);
            }
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String current) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}