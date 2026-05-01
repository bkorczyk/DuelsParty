package org.antix.duelsparty.duel.util;

import org.antix.duelsparty.util.MessageService;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {

    @Test
    void shouldFallbackToEnglishWhenPolishIsMissing() {
        MessageService service = new MessageService();

        // Ładujemy tylko angielski
        service.loadLanguage("en", Map.of("error.no-arenas", "&cNo arenas found!"));

        // Prosimy o polski, którego nie ma
        String message = service.getMessage("pl", "error.no-arenas");

        // Powinien wrócić angielski (z przetworzonym kolorem §c)
        assertEquals("§cNo arenas found!", message);
    }
}