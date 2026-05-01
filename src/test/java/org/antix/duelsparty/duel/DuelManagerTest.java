package org.antix.duelsparty.duel;


import org.antix.duelsparty.DuelException;
import org.antix.duelsparty.duel.arena.Arena;
import org.antix.duelsparty.duel.match.MatchState;
import org.antix.duelsparty.util.MessageService;
import org.junit.jupiter.api.Test;
import org.bukkit.entity.Player;
import org.antix.duelsparty.duel.arena.ArenaLocation;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


class DuelManagerTest {

    @Test
    void shouldCreateNewDuel() {
        DuelManager manager = new DuelManager();
        // Na razie użyjemy nulli zamiast prawdziwych graczy,
        // żeby nie komplikować sobie życia Mockito na samym starcie
        Duel duel = manager.createDuel(null, null);

        assertNotNull(duel, "Pojedynek powinien zostać utworzony!");
    }

    @Test
    void duelShouldHaveActiveStateInitially() {
        DuelManager manager = new DuelManager();
        Duel duel = manager.createDuel(null, null);

        // Chcemy, aby nowy pojedynek od razu był w stanie STARTING
        assertEquals(MatchState.STARTING, duel.getState());
    }
    @Test
    void shouldThrowExceptionWhenPlayersAreTheSame() {
        DuelManager manager = new DuelManager();
        // Tworzymy dwóch "udawanych" graczy
        Player player = mock(Player.class);

        // Oczekujemy, że manager rzuci błąd (IllegalArgumentException)
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createDuel(player, player);
        });
    }

    @Test
    void shouldAssignArenaToNewDuel() {
        DuelManager manager = new DuelManager();
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);

        Duel duel = manager.createDuel(p1, p2);

        // To wymusi na nas stworzenie klasy Arena i metody getArena()
        assertNotNull(duel.getArena(), "Pojedynek musi mieć przypisaną arenę!");
    }
    /*
    @Test
    void shouldNotAssignTheSameArenaTwice() {
        DuelManager manager = new DuelManager();
        Arena arena = new Arena("Pustynia");
        manager.addArena(arena); // Musimy dodać metodę do rejestrowania aren

        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player p3 = mock(Player.class);
        Player p4 = mock(Player.class);

        // Pierwszy pojedynek zajmuje arenę
        Duel duel1 = manager.createDuel(p1, p2);

        // Drugi pojedynek nie powinien móc wystartować na tej samej arenie
        // (Możemy oczekiwać błędu lub null, ale profesjonalnie rzucamy błąd)
        assertThrows(IllegalStateException.class, () -> {
            manager.createDuel(p3, p4);
        }, "Nie powinno być wolnych aren!");
    }
    */

    @Test
    void shouldReturnTranslatedMessage() {
        // W prawdziwym kodzie dane będą z pliku yml
        MessageService service = new MessageService();
        service.loadLanguage("pl", Map.of("duel-start", "Walka się rozpoczęła!"));

        assertEquals("Walka się rozpoczęła!", service.getMessage("pl", "duel-start"));
    }

    private Arena createTestArena(String name) {
        ArenaLocation loc1 = new ArenaLocation("world", 0, 64, 0, 0, 0);
        ArenaLocation loc2 = new ArenaLocation("world", 10, 64, 10, 0, 0);
        return new Arena(name, loc1, loc2);
    }

    @Test
    void shouldAssignSpecificArenaWhenProvided() {
        DuelManager manager = new DuelManager();
        Arena arena1 = createTestArena("Arena-1");
        Arena arena2 = createTestArena("Arena-2");
        manager.addArena(arena1);
        manager.addArena(arena2);

        // Gracz jawnie wybiera Arena-2
        Duel duel = manager.createDuel(mock(Player.class), mock(Player.class), arena2);

        assertEquals("Arena-2", duel.getArena().getName());
    }
/*
    @Test
    void shouldAssignRandomArenaWhenNoneProvided() {
        DuelManager manager = new DuelManager();
        manager.addArena(new Arena("A1"));
        manager.addArena(new Arena("A2"));

        // Przekazujemy null jako arenę -> oczekujemy losowania
        Duel duel = manager.createDuel(mock(Player.class), mock(Player.class), null);

        assertNotNull(duel.getArena());
    }
    @Test
    void shouldThrowCorrectKeyWhenNoArenas() {
        DuelManager manager = new DuelManager();
        // Nie dodajemy żadnych aren...

        DuelException ex = assertThrows(DuelException.class, () -> {
            manager.createDuel(mock(Player.class), mock(Player.class), null);
        });

        assertEquals("error.no-arenas-available", ex.getMessageKey());
    }

 */
    @Test
    void shouldThrowExceptionWhenPlayersAreNull() {
        DuelManager manager = new DuelManager();

        DuelException ex = assertThrows(DuelException.class, () -> {
            manager.createDuel(null, null);
        });

        assertEquals("error.player-null", ex.getMessageKey());
    }
    @Test
    void shouldCreateNewDuelWithSpawnPoints() {
        DuelManager manager = new DuelManager();

        // Tworzymy udawane lokalizacje
        ArenaLocation loc1 = new ArenaLocation("world", 100, 64, 100, 0, 0);
        ArenaLocation loc2 = new ArenaLocation("world", 120, 64, 120, 180, 0);

        Arena arena = new Arena("Pustynia", loc1, loc2);
        manager.addArena(arena);

        Duel duel = manager.createDuel(mock(Player.class), mock(Player.class));

        assertEquals(loc1, duel.getArena().getSpawn1());
        assertEquals("world", duel.getArena().getSpawn1().worldName());
    }
}