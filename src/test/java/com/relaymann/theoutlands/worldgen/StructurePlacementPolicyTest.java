package com.relaymann.theoutlands.worldgen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StructurePlacementPolicyTest {
    @Test void cellAnchorIsDeterministicAndInsideItsCell() {
        int[] first = StructurePlacementPolicy.anchorForCell(1234L, -3, 8);
        assertArrayEquals(first, StructurePlacementPolicy.anchorForCell(1234L, -3, 8));
        assertTrue(first[0] >= -24 && first[0] < -16);
        assertTrue(first[1] >= 64 && first[1] < 72);
    }

    @Test void onlyTheSelectedChunkCanAnchorItsCell() {
        int[] anchor = StructurePlacementPolicy.anchorForCell(99L, -2, 4);
        int count = 0;
        for (int x = -16; x < -8; x++) for (int z = 32; z < 40; z++)
            if (StructurePlacementPolicy.isSpacedAnchor(99L, x, z, 4)) count++;
        assertTrue(count <= 1);
        assertTrue(anchor[0] >= -16 && anchor[0] < -8);
        assertTrue(anchor[1] >= 32 && anchor[1] < 40);
    }

    @Test void nearbyCandidateAnchorsCannotBothWinSpacing() {
        long seed = 987654321L;
        for (int cellX = -4; cellX <= 4; cellX++) for (int cellZ = -4; cellZ <= 4; cellZ++) {
            int[] a = StructurePlacementPolicy.anchorForCell(seed, cellX, cellZ);
            int[] b = StructurePlacementPolicy.anchorForCell(seed, cellX + 1, cellZ);
            if (Math.max(Math.abs(a[0] - b[0]), Math.abs(a[1] - b[1])) < 4)
                assertFalse(StructurePlacementPolicy.isSpacedAnchor(seed, a[0], a[1], 4)
                        && StructurePlacementPolicy.isSpacedAnchor(seed, b[0], b[1], 4));
        }
    }

    @Test void chunkClippingIncludesEdgesAndRejectsEveryOutsideAxis() {
        assertTrue(StructurePlacementPolicy.containsBlock(0, 10, 20, 15, 30, 35, 0, 10, 20));
        assertTrue(StructurePlacementPolicy.containsBlock(0, 10, 20, 15, 30, 35, 15, 30, 35));
        assertFalse(StructurePlacementPolicy.containsBlock(0, 10, 20, 15, 30, 35, -1, 20, 25));
        assertFalse(StructurePlacementPolicy.containsBlock(0, 10, 20, 15, 30, 35, 8, 9, 25));
        assertFalse(StructurePlacementPolicy.containsBlock(0, 10, 20, 15, 30, 35, 8, 20, 36));
    }

    @Test void chunkRandomSeedIsStableAndPositionSpecific() {
        assertEquals(StructurePlacementPolicy.chunkSeed(1L, -20, 40),
                StructurePlacementPolicy.chunkSeed(1L, -20, 40));
        assertNotEquals(StructurePlacementPolicy.chunkSeed(1L, -20, 40),
                StructurePlacementPolicy.chunkSeed(1L, -19, 40));
    }
}
