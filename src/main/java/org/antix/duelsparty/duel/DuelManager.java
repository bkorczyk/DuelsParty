package org.antix.duelsparty.duel;

import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.duel.kit.Kit;
import org.antix.duelsparty.duel.kit.KitManager; // Import managera kitów
import org.antix.duelsparty.util.ArenaLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.*;

public class DuelManager {
    // 1. Dodajemy pole i oznaczamy jako final (bezpieczeństwo wątkowe)
    private final KitManager kitManager;
    private final List<Arena> arenas = new ArrayList<>();
    private final Map<UUID, InviteContext> pendingInvites = new HashMap<>();
    private final List<Duel> activeDuels = new ArrayList<>();

    // 2. Poprawiamy konstruktor - iniekcja zależności
    public DuelManager(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    public void addArena(Arena arena) {
        arenas.add(arena);
    }

    public Optional<Arena> getArenaByName(String name) {
        return arenas.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Główna metoda tworząca pojedynek.
     */
    public Duel createDuel(List<Player> teamA, List<Player> teamB, Arena requestedArena, String kitId) {
        DuelsPartyPlugin.debug("Inicjacja procesu createDuel z kitem: " + kitId);

        validateTeam(teamA);
        validateTeam(teamB);

        // 3. Teraz kitManager jest dostępny!
        Kit selectedKit = kitManager.getKit(kitId)
                .orElseGet(() -> kitManager.getKit("default")
                        .orElseThrow(() -> new DuelException("error.no-kits-loaded")));

        Arena arena = (requestedArena != null && !requestedArena.isBusy()) ?
                requestedArena : arenas.stream()
                .filter(a -> !a.isBusy())
                .findAny()
                .orElseThrow(() -> new DuelException("error.no-arenas-available"));

        arena.setBusy(true);
        // Przekazujemy Kit do obiektu Duel
        Duel duel = new Duel(teamA, teamB, arena, selectedKit);
        activeDuels.add(duel);

        DuelsPartyPlugin.debug("Pojedynek stworzony. Arena: " + arena.getName() + ", Kit: " + selectedKit.id());
        return duel;
    }

    /**
     * Poprawione przeciążenie dla 1v1 - używa domyślnego kitu
     */
    public Duel createDuel(Player p1, Player p2, Arena requestedArena) {
        return createDuel(Collections.singletonList(p1), Collections.singletonList(p2), requestedArena, "default");
    }

    private void validateTeam(List<Player> team) {
        if (team == null || team.isEmpty()) {
            throw new DuelException("error.player-offline");
        }

        for (Player p : team) {
            if (p == null || !p.isOnline()) {
                throw new DuelException("error.player-offline");
            }

            if (getDuelByPlayer(p).isPresent()) {
                throw new DuelException("error.player-already-in-fight");
            }
        }
    }

    // --- Reszta metod (sendInvite, getInvite, removeDuel, save/load) bez zmian ---

    // Zlokalizuj metodę sendInvite w DuelManager.java i podmień ją na tę wersję:
    public void sendInvite(Player sender, Player target, Arena arena, String kitId) {
        if (sender.equals(target)) throw new DuelException("error.self-duel");

        // Dodajemy kitId do konstruktora rekordu (wymagane 4 parametry)
        pendingInvites.put(target.getUniqueId(), new InviteContext(
                sender.getUniqueId(),
                arena,
                kitId != null ? kitId : "default", // Zabezpieczenie: jeśli kitId jest null, dajemy default
                System.currentTimeMillis()
        ));
        DuelsPartyPlugin.debug("Invite stored: " + sender.getName() + " -> " + target.getName() + " (Kit: " + kitId + ")");
    }

    public Optional<InviteContext> getInvite(UUID uuid) {
        return Optional.ofNullable(pendingInvites.get(uuid));
    }

    public void removeInvite(UUID uuid) {
        pendingInvites.remove(uuid);
    }

    public Optional<Duel> getDuelByPlayer(Player player) {
        return activeDuels.stream()
                .filter(duel -> duel.containsPlayer(player))
                .findFirst();
    }

    public void removeDuel(Duel duel) {
        if (duel.getArena() != null) {
            duel.getArena().setBusy(false);
        }
        activeDuels.remove(duel);
        DuelsPartyPlugin.debug("Duel removed from active list.");
    }

    public void saveArenas(FileConfiguration config) {
        config.set("arenas", null);
        for (Arena arena : arenas) {
            String path = "arenas." + arena.getName();
            config.set(path + ".spawn1", arena.spawn1().serialize());
            config.set(path + ".spawn2", arena.spawn2().serialize());
        }
    }

    public void loadArenas(FileConfiguration config) {
        if (!config.contains("arenas")) return;
        arenas.clear();
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            String path = "arenas." + name;
            ArenaLocation s1 = ArenaLocation.deserialize(config.getString(path + ".spawn1"));
            ArenaLocation s2 = ArenaLocation.deserialize(config.getString(path + ".spawn2"));

            if (s1 != null && s2 != null) {
                arenas.add(new Arena(name, s1, s2));
            }
        }
        DuelsPartyPlugin.debug("Loaded " + arenas.size() + " arenas from config.");
    }
    public List<String> getArenaNames() {
        return arenas.stream().map(Arena::getName).toList();
    }

    public void removeArena(String name) {
        arenas.removeIf(arena -> arena.getName().equalsIgnoreCase(name));
    }
}