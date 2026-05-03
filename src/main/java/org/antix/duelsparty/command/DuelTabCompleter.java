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
import java.util.List;
import java.util.stream.Collectors;

public class DuelTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Podpowiadaj graczy online (z wyłączeniem siebie)
            String current = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .filter(name -> !name.equals(sender.getName()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // Podpowiadaj areny
            String current = args[1].toLowerCase();
            // Uwaga: Zakładam, że masz metodę getArenas() lub podobną w DuelManager
            // Jeśli nie, musimy ją dodać do DuelManager
            return DuelsPartyPlugin.getInstance().getDuelManager().getArenaNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            // Podpowiadaj kity
            String current = args[2].toLowerCase();
            return DuelsPartyPlugin.getInstance().getKitManager().getKitNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}