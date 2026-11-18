package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorrorMemoryTest {
    @Test
    void boundsRecentHistoryWithoutLosingOrder() {
        List<String> history = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            history = HorrorMemoryPolicy.appendEncounter(history, "event_" + i, 32);
        }
        assertEquals(32, history.size());
        assertEquals("event_8", history.getFirst());
        assertEquals("event_39", history.getLast());
    }

    @Test
    void discoveriesAreUniqueAndOrdered() {
        List<String> discoveries = List.of();
        discoveries = HorrorMemoryPolicy.addUnique(discoveries, "first_symbol");
        discoveries = HorrorMemoryPolicy.addUnique(discoveries, "first_symbol");
        discoveries = HorrorMemoryPolicy.addUnique(discoveries, "hidden_record");
        assertEquals(List.of("first_symbol", "hidden_record"), discoveries);
    }

    @Test
    void rejectsInvalidIdentifiers() {
        assertFalse(HorrorMemoryPolicy.isValidId(null));
        assertFalse(HorrorMemoryPolicy.isValidId(""));
        assertFalse(HorrorMemoryPolicy.isValidId(" "));
        assertFalse(HorrorMemoryPolicy.isValidId("x".repeat(129)));
        assertTrue(HorrorMemoryPolicy.isValidId("death_jar_fragment"));
    }
}
