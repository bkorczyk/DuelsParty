package org.antix.duelsparty;

import org.antix.duelsparty.command.DuelAcceptCommand;
import org.antix.duelsparty.command.DuelInviteCommand;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelsPartyPlugin extends JavaPlugin {

    // Tworzymy instancje naszych serwisów
    private final DuelManager duelManager = new DuelManager();
    private final MessageService messageService = new MessageService();

    @Override
    public void onEnable() {
        // Rejestracja komend z przekazaniem instancji managerów
        getCommand("duel").setExecutor(new DuelInviteCommand(duelManager, messageService));
        getCommand("accept").setExecutor(new DuelAcceptCommand(duelManager, messageService));

        // Log informacyjny w konsoli
        getLogger().info("DuelsParty has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DuelsParty has been disabled!");
    }
}