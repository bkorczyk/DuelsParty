package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaAdminTabCompleter implements TabCompleter {

    // Główne gałęzie administracyjne
    private static final List<String> MAIN_COMMANDS = Arrays.asList("arena", "kit", "reload");
    // Podkomendy dla /da arena
    private static final List<String> ARENA_ACTIONS = Arrays.asList("set1", "set2", "delete", "tp");
    // Podkomendy dla /da kit
    private static final List<String> KIT_ACTIONS = Arrays.asList("save", "delete", "list");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("duelsparty.admin")) return List.of();

        // Definiujemy 'current' na początku, aby była widoczna w całej metodzie
        String current = args[args.length - 1].toLowerCase();

        // 1. Podpowiedzi dla głównego poziomu: /da <arena|kit|reload>
        if (args.length == 1) {
            return filter(MAIN_COMMANDS, current);
        }

        // 2. Logika dla modułu ARENA: /da arena <action>
        if (args[0].equalsIgnoreCase("arena")) {
            if (args.length == 2) {
                return filter(ARENA_ACTIONS, current);
            }

            // Podpowiedzi nazw aren dla /da arena <delete|tp|set2> <nazwa>
            if (args.length == 3) {
                String subAction = args[1].toLowerCase();
                if (subAction.equals("delete") || subAction.equals("tp") || subAction.equals("set2")) {
                    return filter(DuelsPartyPlugin.getInstance().getDuelManager().getArenaNames(), current);
                }
            }
        }

        // 3. Logika dla modułu KIT: /da kit <action>
        if (args[0].equalsIgnoreCase("kit")) {
            if (args.length == 2) {
                return filter(KIT_ACTIONS, current);
            }

            // Podpowiedzi nazw zestawów dla /da kit <delete|save> <nazwa>
            if (args.length == 3) {
                return filter(DuelsPartyPlugin.getInstance().getKitManager().getKitNames().stream().toList(), current);
            }
        }

        return List.of();
    }

    /**
     * Uniwersalna metoda filtrująca podpowiedzi
     */
    private List<String> filter(List<String> list, String current) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}