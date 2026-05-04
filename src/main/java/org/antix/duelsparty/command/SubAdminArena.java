package org.antix.duelsparty.command;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.util.ArenaLocation;
import org.bukkit.entity.Player;
import org.antix.duelsparty.util.MessageService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Obsługuje sub-komendę /da arena <action> <name>.
 * Implementuje logikę tworzenia, usuwania i zarządzania arenami.
 */
public class SubAdminArena implements SubCommand {

    private final DuelManager duelManager;
    private final MessageService messageService;
    // Pamięć sesji tworzenia areny
    private final Map<UUID, ArenaLocation> pendingSpawn1 = new HashMap<>();

    public SubAdminArena(DuelManager duelManager, MessageService messageService) {
        this.duelManager = duelManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Player player, String[] args, String lang) {
        if (args.length < 2) {
            player.sendMessage(messageService.getMessage(lang, "usage-arena"));
            return;
        }


        String action = args[1].toLowerCase();
        UUID uuid = player.getUniqueId();

        switch (action) {
            case "set1" -> {
                pendingSpawn1.put(uuid, ArenaLocation.fromLocation(player.getLocation()));
                player.sendMessage(messageService.getMessage(lang, "spawn-saved"));
            }
            case "set2" -> handleSet2(player, args,lang);
            case "delete" -> handleDelete(player, args,lang);
            case "tp" -> handleTeleport(player, args,lang);
            default -> player.sendMessage(messageService.getMessage(lang, "unknown-arena-action"));
        }
    }

    private void handleSet2(Player player, String[] args, String lang) {
        if (args.length < 3) {
            player.sendMessage(messageService.getMessage(lang,"arena-not-defined"));
            return;
        }

        ArenaLocation s1 = pendingSpawn1.remove(player.getUniqueId());
        if (s1 == null) {
            player.sendMessage(messageService.getMessage(lang,"missing-spawn-1"));
            return;
        }

        String name = args[2];
        ArenaLocation s2 = ArenaLocation.fromLocation(player.getLocation());
        Arena newArena = new Arena(name, s1, s2);

        duelManager.addArena(newArena);


        saveAsync(player, messageService.getMessage(lang,"arena-created") + name);
    }

    private void handleDelete(Player player, String[] args, String lang) {
        if (args.length < 3) {
            player.sendMessage(messageService.getMessage(lang,"arena-not-defined"));
            return;
        }
        String name = args[2];
        duelManager.removeArena(name);
        saveAsync(player,messageService.getMessage(lang,"arena-deleted") + name);
    }

    private void handleTeleport(Player player, String[] args, String lang) {
        if (args.length < 3) return;
        duelManager.getArenaByName(args[2]).ifPresentOrElse(
                arena -> player.teleport(arena.spawn1().toLocation()),
        () -> player.sendMessage(messageService.getMessage(lang,"arena-not-exists"))
        );
    }

    /**
     * Złota Reguła: Zapisujemy dane asynchronicznie, by nie mrozić serwera (I/O).
     */
    private void saveAsync(Player player, String successMsg) {
        DuelsPartyPlugin plugin = DuelsPartyPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            duelManager.saveArenas(plugin.getConfig());
            plugin.saveConfig();
            player.sendMessage("§6§l» " + successMsg);
        });
    }

    public void clearSession(UUID uuid) {
        pendingSpawn1.remove(uuid);
    }
}