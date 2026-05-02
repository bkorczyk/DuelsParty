package org.antix.duelsparty;

import org.antix.duelsparty.command.DuelAcceptCommand;
import org.antix.duelsparty.command.DuelInviteCommand;
import org.antix.duelsparty.duel.DuelListener;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelsPartyPlugin extends JavaPlugin {

    // Instancje serwisów - inicjalizujemy je tutaj, aby były unikalne dla całego pluginu
    private final DuelManager duelManager = new DuelManager();
    private final MessageService messageService = new MessageService();

    @Override
    public void onEnable() {
        // 1. Rejestracja komend
        registerCommands();

        // 2. Rejestracja Listenera (Bardzo ważne! Bez tego hity i śmierć nie będą działać)
        getServer().getPluginManager().registerEvents(new DuelListener(duelManager), this);

        // 3. Log powitalny
        getLogger().info("================================");
        getLogger().info(" DuelsParty Plugin Enabled! ");
        getLogger().info(" Autor: Antix ");
        getLogger().info("================================");
    }

    private void registerCommands() {
        // Używamy pomocniczej metody, żeby kod był czystszy i bezpieczniejszy
        registerCommand("duel", new DuelInviteCommand(duelManager, messageService));
        registerCommand("accept", new DuelAcceptCommand(duelManager, messageService));
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().severe("Błąd! Komenda /" + name + " nie została znaleziona w plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("DuelsParty has been disabled! Zapisywanie danych...");
    }

    // Gettery (przydadzą się w przyszłości)
    public DuelManager getDuelManager() { return duelManager; }
    public MessageService getMessageService() { return messageService; }
}