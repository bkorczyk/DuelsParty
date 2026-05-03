package org.antix.duelsparty.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageService {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final String defaultLang = "en";

    public MessageService(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLanguage("en");
        loadLanguage("pl");
    }

    public void loadLanguage(String lang) {
        File file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("messages_" + lang + ".yml", false);
        }
        configs.put(lang, YamlConfiguration.loadConfiguration(file));
    }

    public String getMessage(String lang, String key) {
        // Próba pobrania wiadomości w języku gracza, fallback do angielskiego
        FileConfiguration config = configs.getOrDefault(lang, configs.get(defaultLang));
        String message = config.getString("messages." + key);

        if (message == null && !lang.equals(defaultLang)) {
            message = configs.get(defaultLang).getString("messages." + key);
        }

        if (message == null) return ChatColor.RED + "Missing key: " + key;

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}