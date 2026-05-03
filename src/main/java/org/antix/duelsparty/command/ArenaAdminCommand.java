package org.antix.duelsparty.command;

import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.ArenaLocation;
import org.antix.duelsparty.duel.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaAdminCommand implements CommandExecutor {
    private final DuelManager duelManager;

    // Mapa przechowująca pierwszy punkt spawnu do czasu ustawienia drugiego
    private final Map<UUID, ArenaLocation> pendingSpawn1 = new HashMap<>();

    public ArenaAdminCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy!");
            return true;
        }

        UUID uuid = player.getUniqueId(); // Używamy UUID zamiast obiektu Player

        if (args.length < 2) {
            player.sendMessage("§6§l» §cPoprawne użycie: §f/da <set1|set2> <nazwa>");
            return true;
        }

        String action = args[0].toLowerCase();
        String arenaName = args[1];

        switch (action) {
            case "set1" -> {
                ArenaLocation loc1 = ArenaLocation.fromLocation(player.getLocation());
                pendingSpawn1.put(uuid, loc1); // Zapisujemy po UUID
                player.sendMessage("§6§l» §aPomyślnie ustawiono §fSpawn 1.");
            }
            case "set2" -> {
                if (!pendingSpawn1.containsKey(uuid)) {
                    player.sendMessage("§6§l» §cBłąd! Najpierw użyj set1.");
                    return true;
                }
                ArenaLocation s1 = pendingSpawn1.remove(player);
                ArenaLocation s2 = ArenaLocation.fromLocation(player.getLocation());

                // Tworzymy arenę i dodajemy do managera
                Arena newArena = new Arena(arenaName, s1, s2);
                duelManager.addArena(newArena);

                player.sendMessage("§6§l» §6Arena §f" + arenaName + " §6została poprawnie utworzona!");
                player.sendMessage("§7Możesz teraz wyzwać gracza wpisując: §f/duel <nick> " + arenaName);
            }
            default -> player.sendMessage("§6§l» §cNieznana akcja! Użyj §fset1 §club §fset2§c.");
        }

        return true;
    }
    // Metoda pomocnicza dla Listenera
    public void clearPendingSession(UUID uuid) {
        pendingSpawn1.remove(uuid);
    }
}