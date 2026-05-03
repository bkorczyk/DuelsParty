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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String current = args[args.length - 1].toLowerCase();

        // 1. Podpowiedzi dla pierwszego argumentu: Nicki LUB Sub-komendy
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(SUB_COMMANDS);

            // Dodajemy graczy online (z pominięciem wysyłającego)
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    suggestions.add(p.getName());
                }
            });

            return filter(suggestions, current);
        }


        if (!SUB_COMMANDS.contains(args[0].toLowerCase())) {


            if (args.length == 2) {
                return filter(DuelsPartyPlugin.getInstance().getDuelManager().getArenaNames(), current);
            }

            if (args.length == 3) {
                return filter(DuelsPartyPlugin.getInstance().getKitManager().getKitNames().stream().toList(), current);
            }
        }

        // 3. Obsługa sub-komendy party (KROK 2 - szkielet)
        if (args[0].equalsIgnoreCase("party") && args.length == 2) {
            return filter(Arrays.asList("invite", "join", "leave", "kick"), current);
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String current) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}