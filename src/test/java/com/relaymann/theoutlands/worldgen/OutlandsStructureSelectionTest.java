package com.relaymann.theoutlands.worldgen;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class OutlandsStructureSelectionTest {
    private static final Set<String> IDS = Set.of(
            "cabin", "tower", "bunker", "shrine", "observatory", "archive",
            "research_facility", "apostle_cathedral", "forsaken_settlement",
            "last_outpost", "forgotten_station", "null_chamber", "hollow_crypt",
            "boundary_monument", "ranger_station", "mining_operation", "ruined_settlement",
            "observation_tower", "subterranean_complex", "nightmare_signal_hollow");

    @Test
    void minimumDistanceAndDisabledFrequencyRejectSites() {
        assertNull(StructureLayoutPolicy.chooseLayout(7L, 999.0D, true, 1_000.0D, 1.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(7L, 10_000.0D, true, 1_000.0D, 0.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(7L, 1_000.0D, false, 1_001.0D, 1.0D));
    }

    @Test
    void forestOnlyCabinRequiresForest() {
        boolean foundCabin = false;
        for (long seed = 0; seed < 10_000 && !foundCabin; seed++) {
            assertNull(StructureLayoutPolicy.chooseLayout(seed, 1_000.0D, false, 1_000.0D, 1.0D));
            String selected = StructureLayoutPolicy.chooseLayout(seed, 1_000.0D, true, 1_000.0D, 1.0D);
            if (selected != null) {
                assertEquals("cabin", selected);
                foundCabin = true;
            }
        }
        assertTrue(foundCabin, "The forest cabin should be reachable at its minimum distance");
    }


    @Test
    void nightmareStructureAnchorsKeepFootprintsFromOverlapping() {
        for (long seed = 0; seed < 5; seed++) {
            java.util.List<int[]> anchors = new java.util.ArrayList<>();
            for (int cellX = -8; cellX <= 8; cellX++) {
                for (int cellZ = -8; cellZ <= 8; cellZ++) {
                    int[] point = StructurePlacementPolicy.anchorForCell(seed, cellX, cellZ, 4);
                    if (StructurePlacementPolicy.isSpacedAnchor(seed, point[0], point[1], 3, 4)) {
                        anchors.add(point);
                    }
                }
            }
            for (int i = 0; i < anchors.size(); i++) {
                for (int j = i + 1; j < anchors.size(); j++) {
                    int[] a = anchors.get(i), b = anchors.get(j);
                    assertTrue(Math.max(Math.abs(a[0] - b[0]), Math.abs(a[1] - b[1])) >= 3,
                            "NIGHTMARE structure anchors must leave at least 48 blocks between footprints");
                }
            }
        }
    }

    @Test
    void nightmareLayoutsAreExclusiveAndSelectable() {
        Set<String> nightmareOnly = Set.of(
                "nightmare_containment", "blood_moon_shrine", "nightmare_outpost", "black_site");
        boolean foundNightmare = false;
        for (long seed = 0; seed < 20_000 && !foundNightmare; seed++) {
            String survival = StructureLayoutPolicy.chooseLayout(seed, 60_000.0D, true, 250.0D, 2.5D);
            assertTrue(survival == null || !nightmareOnly.contains(survival),
                    "Nightmare-only site leaked into Survival");
            String nightmare = StructureLayoutPolicy.chooseLayout(
                    seed, 60_000.0D, true, 250.0D, 2.5D, true);
            foundNightmare = nightmare != null && nightmareOnly.contains(nightmare);
        }
        assertTrue(foundNightmare, "NIGHTMARE should be able to select an exclusive structure");
    }

    @Test
    void seededLayoutSelectionIsRepeatable() {
        boolean found = false;
        for (long seed = 0; seed < 10_000 && !found; seed++) {
            String first = StructureLayoutPolicy.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D);
            String second = StructureLayoutPolicy.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D);
            assertEquals(first, second);
            if (first != null) {
                assertTrue(IDS.contains(first), "Unknown structure identity selected: " + first);
                found = true;
            }
        }
        assertTrue(found, "A seeded structure layout should be selectable in the deep Outlands");
    }

    @Test
    void nightmareSignalHollowIsExclusiveToNightmareWorlds() {
        boolean foundNightmareSite = false;
        for (long seed = 0; seed < 30_000 && !foundNightmareSite; seed++) {
            assertNotEquals("nightmare_signal_hollow",
                    StructureLayoutPolicy.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D, false));
            foundNightmareSite = "nightmare_signal_hollow".equals(
                    StructureLayoutPolicy.chooseLayout(seed, 60_000.0D, true, 1_000.0D, 1.0D, true));
        }
        assertTrue(foundNightmareSite, "Expected the NIGHTMARE-only hollow to be selectable");
    }

    @Test
    void layoutSelectionRejectsInvalidNumericInputs() {
        assertNull(StructureLayoutPolicy.chooseLayout(1L, Double.NaN, true, 1_000.0D, 1.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(1L, Double.POSITIVE_INFINITY, true, 1_000.0D, 1.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(1L, -1.0D, true, 1_000.0D, 1.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(1L, 5_000.0D, true, Double.NaN, 1.0D));
        assertNull(StructureLayoutPolicy.chooseLayout(1L, 5_000.0D, true, 1_000.0D, Double.NaN));
        assertNull(StructureLayoutPolicy.chooseLayout(1L, 5_000.0D, true, 1_000.0D, Double.POSITIVE_INFINITY));
    }
}
