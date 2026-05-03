package org.antix.duelsparty.duel.kit;

import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Map;

public record Kit(
        String id,
        String displayName,
        List<ItemStack> content,    // Główny ekwipunek
        ItemStack[] armor,          // Zbroja (4 sloty)
        ItemStack offhand,          // Tarcza/Totem
        Map<String, Object> settings // Dodatkowe opcje np. budowanie, mikstury
) {}