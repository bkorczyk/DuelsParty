package org.antix.duelsparty;


import org.antix.duelsparty.command.*;
import org.antix.duelsparty.database.DatabaseService;
import org.antix.duelsparty.database.MySQLDatabase;
import org.antix.duelsparty.database.NullDatabase;
import org.antix.duelsparty.duel.kit.KitManager;
import org.antix.duelsparty.party.PartyManager;
import org.antix.duelsparty.duel.DuelListener;
import org.antix.duelsparty.duel.DuelManager;
import org.antix.duelsparty.util.MessageService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuelsPartyPlugin extends JavaPlugin {

    private DuelManager duelManager;
    private KitManager kitManager;
    private MessageService messageService;
    private PartyManager partyManager;
    private static DuelsPartyPlugin instance;
    private DatabaseService databaseService;


    @Override
    public void onEnable() {
        instance = this;

        setupDatabase();
        this.messageService = new MessageService(this);
        this.partyManager = new PartyManager(this);
        this.kitManager = new KitManager(this);

        this.kitManager.loadKits();
        this.duelManager = new DuelManager(kitManager);

        PluginCommand duelCmd = getCommand("duel");
        if (duelCmd != null) {
            duelCmd.setTabCompleter(new DuelTabCompleter());
        }

        duelManager.loadArenas(getConfig());

        registerCommands();
        getServer().getPluginManager().registerEvents(new DuelListener(duelManager, messageService), this);
        showWelcomeMessage();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Zapisywanie aren...");
        duelManager.saveArenas(getConfig());
        saveConfig();
    }

    private void registerCommands() {
        DuelCommand duelCommand = new DuelCommand(duelManager, messageService);
        registerCommand("duel", duelCommand);

        if (getCommand("duel") != null) {
            getCommand("duel").setTabCompleter(new DuelTabCompleter());
        }

        DuelAdminCommand adminCommand = new DuelAdminCommand(duelManager, messageService);
        registerCommand("dueladmin", adminCommand);

        if (getCommand("dueladmin") != null) {
            getCommand("dueladmin").setTabCompleter(new DuelAdminTabCompleter());
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().severe("Błąd! Komenda /" + name + " nie została znaleziona w plugin.yml!");
        }
    }

    private void showWelcomeMessage() {
        getLogger().info("================================");
        getLogger().info(" DuelsParty Plugin Enabled! ");
        getLogger().info(" Autor: Antix ");
        getLogger().info("================================");
    }
    public static void debug(String message) {
        if (instance != null && instance.getConfig().getBoolean("debug", false)) {
            instance.getLogger().info("[DEBUG] " + message);
        }
    }
    public static DuelsPartyPlugin getInstance() {
        return instance;
    }
    public DuelManager getDuelManager() { return duelManager; }
    public MessageService getMessageService() { return messageService; }
    public KitManager getKitManager(){return kitManager;}

    private void setupDatabase() {
        if (getConfig().getBoolean("mysql.enabled", false)) {
            this.databaseService = new MySQLDatabase(
                    getConfig().getString("mysql.host"),
                    getConfig().getInt("mysql.port"),
                    getConfig().getString("mysql.database"),
                    getConfig().getString("mysql.user"),
                    getConfig().getString("mysql.password")
            );
            getLogger().info("System bazy danych MySQL został uruchomiony.");
        } else {
            this.databaseService = new NullDatabase();
            getLogger().info("Baza danych jest wyłączona. Statystyki nie będą zapisywane.");
        }
    }
    public DatabaseService getDatabaseService(){
        return databaseService;
    }
    public PartyManager getPartyManager(){return partyManager;}
}