package com.relaymann.theoutlands.horror;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HorrorMemoryTest {
    @Test
    void recordsEncounterHistoryAndTotalCount() {
        CompoundTag playerData = new CompoundTag();
        HorrorMemory.recordEncounter(playerData, "distant_figure", 100);
        HorrorMemory.recordEncounter(playerData, "false_footsteps", 140);

        assertTrue(HorrorMemory.hasEncountered(playerData, "distant_figure"));
        assertTrue(HorrorMemory.hasEncountered(playerData, "false_footsteps"));
        assertEquals(2, HorrorMemory.totalEncounters(playerData));
    }

    @Test
    void boundsRecentHistoryWithoutLosingTotalCount() {
        CompoundTag playerData = new CompoundTag();
        for (int i = 0; i < 40; i++) {
            HorrorMemory.recordEncounter(playerData, "event_" + i, i);
        }
        assertEquals(40, HorrorMemory.totalEncounters(playerData));
        assertFalse(HorrorMemory.hasEncountered(playerData, "event_0"));
        assertTrue(HorrorMemory.hasEncountered(playerData, "event_39"));
    }

    @Test
    void discoveriesAreUniqueAndPersistInPlayerData() {
        CompoundTag playerData = new CompoundTag();
        HorrorMemory.recordDiscovery(playerData, "first_symbol");
        HorrorMemory.recordDiscovery(playerData, "first_symbol");
        HorrorMemory.recordDiscovery(playerData, "hidden_record");

        assertTrue(HorrorMemory.hasDiscovered(playerData, "first_symbol"));
        assertTrue(HorrorMemory.hasDiscovered(playerData, "hidden_record"));
        assertFalse(HorrorMemory.hasDiscovered(playerData, "unknown"));
    }

    @Test
    void invalidIdsAreIgnored() {
        CompoundTag playerData = new CompoundTag();
        HorrorMemory.recordEncounter(playerData, " ", 20);
        HorrorMemory.recordDiscovery(playerData, "");
        assertEquals(0, HorrorMemory.totalEncounters(playerData));
        assertFalse(HorrorMemory.hasDiscovered(playerData, ""));
    }
}
