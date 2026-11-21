package com.relaymann.theoutlands.worldgen;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class OutlandsStructureSelectionTest {
    private static final Set<String> IDS = Set.of(
            "cabin", "tower", "bunker", "shrine", "observatory", "archive",
            "research_facility", "apostle_cathedral", "forsaken_settlement",
            "last_outpost", "forgotten_station", "null_chamber", "hollow_crypt",
            "boundary_monument");

    @Test
    void minimumDistanceAndDisabledFrequencyRejectSites() {
        assertNull(OutlandsStructureFeature.chooseLayout(7L, 999.0D, true, 1_000.0D, 1.0D));
        assertNull(OutlandsStructureFeature.chooseLayout(7L, 10_000.0D, true, 1_000.0D, 0.0D));
        assertNull(OutlandsStructureFeature.chooseLayout(7L, 1_000.0D, false, 1_001.0D, 1.0D));
    }

    @Test
    void forestOnlyCabinRequiresForest() {
        boolean foundCabin = false;
        for (long seed = 0; seed < 10_000 && !foundCabin; seed++) {
            assertNull(OutlandsStructureFeature.chooseLayout(seed, 1_000.0D, false, 1_000.0D, 1.0D));
            String selected = OutlandsStructureFeature.chooseLayout(seed, 1_000.0D, true, 1_000.0D, 1.0D);
            if (selected != null) {
                assertEquals("cabin", selected);
                foundCabin = true;
            }
        }
        assertTrue(foundCabin, "The forest cabin should be reachable at its minimum distance");
    }

    @Test
    void seededLayoutSelectionIsRepeatable() {
        boolean found = false;
        for (long seed = 0; seed < 10_000 && !found; seed++) {
            String first = OutlandsStructureFeature.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D);
            String second = OutlandsStructureFeature.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D);
            assertEquals(first, second);
            if (first != null) {
                assertTrue(IDS.contains(first), "Unknown structure identity selected: " + first);
                found = true;
            }
        }
        assertTrue(found, "A seeded structure layout should be selectable in the deep Outlands");
    }
}
