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

    @Test void structureSpacingNeverAllowsOverlappingFootprints() {
        assertEquals(3, StructurePlacementPolicy.minimumSpacingForFootprint(1, 48));
        assertEquals(3, StructurePlacementPolicy.minimumSpacingForFootprint(2, 48));
        assertEquals(4, StructurePlacementPolicy.minimumSpacingForFootprint(4, 48));
        assertEquals(4, StructurePlacementPolicy.minimumSpacingForFootprint(1, 64));
        assertThrows(IllegalArgumentException.class,
                () -> StructurePlacementPolicy.minimumSpacingForFootprint(4, 0));
    }

    @Test void terrainSurfaceToleranceUsesActualGroundHeight() {
        assertTrue(StructurePlacementPolicy.isWithinSurfaceTolerance(65, 65, 5));
        assertTrue(StructurePlacementPolicy.isWithinSurfaceTolerance(60, 65, 5));
        assertFalse(StructurePlacementPolicy.isWithinSurfaceTolerance(59, 65, 5));
        assertFalse(StructurePlacementPolicy.isWithinSurfaceTolerance(70, 65, 4));
        assertFalse(StructurePlacementPolicy.isWithinSurfaceTolerance(65, 65, -1));
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

    @Test void surfaceSamplesCoverTheEntireFootprintIncludingTheLastBlock() {
        assertArrayEquals(new int[] {0,4,8,12,16,20,24,28,32,36,40,44,47},
                StructurePlacementPolicy.surfaceSampleOffsets(48,4));
        assertArrayEquals(new int[] {0}, StructurePlacementPolicy.surfaceSampleOffsets(1,4));
        assertArrayEquals(new int[] {0,3,6,8}, StructurePlacementPolicy.surfaceSampleOffsets(9,3));
        assertThrows(IllegalArgumentException.class,
                () -> StructurePlacementPolicy.surfaceSampleOffsets(0,4));
        assertThrows(IllegalArgumentException.class,
                () -> StructurePlacementPolicy.surfaceSampleOffsets(48,0));
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
