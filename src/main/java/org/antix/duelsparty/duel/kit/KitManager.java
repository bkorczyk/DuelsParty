package org.antix.duelsparty.duel.kit;

import org.antix.duelsparty.DuelsPartyPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager {
    private final DuelsPartyPlugin plugin;
    private final Map<String, Kit> kits = new ConcurrentHashMap<>();

    public KitManager(DuelsPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadKits() {
        File kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) kitsFolder.mkdirs();

        File[] files = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        kits.clear();
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = file.getName().replace(".yml", "");

            // Wersja 1.21.1 automatycznie obsługuje Data Components przy deserializacji ItemStack
            List<ItemStack> content = (List<ItemStack>) config.getList("content", new ArrayList<>());
            ItemStack[] armor = ((List<ItemStack>) config.getList("armor", Arrays.asList(new ItemStack[4])))
                    .toArray(new ItemStack[0]);
            ItemStack offhand = config.getItemStack("offhand", new ItemStack(Material.AIR));

            Kit kit = new Kit(id, config.getString("name", id), content, armor, offhand, new HashMap<>());
            kits.put(id.toLowerCase(), kit);

            DuelsPartyPlugin.debug("Załadowano Kit: " + id + " (Data Components OK)");
        }
    }

    public void applyKit(Player player, String kitId) {
        Kit kit = kits.get(kitId.toLowerCase());
        if (kit == null) {
            DuelsPartyPlugin.debug("BŁĄD: Próba nadania nieistniejącego kitu: " + kitId);
            return;
        }

        PlayerInventory inv = player.getInventory();
        inv.clear();

        // Nadawanie przedmiotów - Paper zachowuje Data Components (Enchants, Potion Effects, Custom Names)
        for (int i = 0; i < kit.content().size(); i++) {
            if (kit.content().get(i) != null) inv.setItem(i, kit.content().get(i).clone());
        }

        inv.setArmorContents(Arrays.stream(kit.armor()).map(item -> item != null ? item.clone() : null).toArray(ItemStack[]::new));
        inv.setItemInOffHand(kit.offhand() != null ? kit.offhand().clone() : null);

        DuelsPartyPlugin.debug("Nadano zestaw " + kitId + " graczowi " + player.getName());
    }

    public Optional<Kit> getKit(String id) {
        return Optional.ofNullable(kits.get(id.toLowerCase()));
    }

    public Set<String> getKitNames() {
        return kits.keySet();
    }
    /**
     * Zwraca wszystkie załadowane zestawy (Kity).
     * Używane m.in. przez GUI wyboru zestawu.
     */
    public Collection<Kit> getKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

}