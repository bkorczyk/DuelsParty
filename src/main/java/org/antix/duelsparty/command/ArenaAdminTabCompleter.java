package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ArenaAdminTabCompleter implements TabCompleter {

    private static final List<String> ACTIONS = List.of("set1", "set2", "delete", "tp");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("duelsparty.admin")) return List.of();

        if (args.length == 1) {
            return ACTIONS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();
            // Dla delete i tp podpowiadamy istniejące areny
            if (action.equals("delete") || action.equals("tp") || action.equals("set2")) {
                return DuelsPartyPlugin.getInstance().getDuelManager().getArenaNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        return List.of();
    }
}