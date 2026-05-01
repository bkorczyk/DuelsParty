package org.antix.duelsparty.util;

import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.Map;

public class MessageService {
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    private static final String DEFAULT_LANG = "en";

    public void loadLanguage(String lang, Map<String, String> messages) {
        translations.put(lang, messages);
    }

    public String getMessage(String lang, String key) {
        // 1. Szukaj w wybranym języku
        String message = translations.getOrDefault(lang, new HashMap<>()).get(key);

        // 2. Fallback do angielskiego, jeśli nie znaleziono
        if (message == null && !lang.equals(DEFAULT_LANG)) {
            message = translations.getOrDefault(DEFAULT_LANG, new HashMap<>()).get(key);
        }

        // 3. Ostateczny fallback, jeśli klucza nigdzie nie ma
        if (message == null) {
            return ChatColor.RED + "Missing key: " + key;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}