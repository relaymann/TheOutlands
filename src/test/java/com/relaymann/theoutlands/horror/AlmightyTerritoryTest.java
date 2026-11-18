package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlmightyTerritoryTest {
    @Test
    void usesActualBorderCenterRatherThanWorldOrigin() {
        assertTrue(AlmightyTerritory.contains(12_000, -8_000, 100_000,
                56_000, -8_000, 5_000));
        assertFalse(AlmightyTerritory.contains(12_000, -8_000, 100_000,
                54_000, -8_000, 5_000));
    }

    @Test
    void identifiesAllFourSidesAndCorners() {
        double centerX = 300;
        double centerZ = -700;
        double size = 20_000;
        assertTrue(AlmightyTerritory.contains(centerX, centerZ, size, 5_500, centerZ, 5_000));
        assertTrue(AlmightyTerritory.contains(centerX, centerZ, size, -4_900, centerZ, 5_000));
        assertTrue(AlmightyTerritory.contains(centerX, centerZ, size, centerX, 4_300, 5_000));
        assertTrue(AlmightyTerritory.contains(centerX, centerZ, size, centerX, -5_600, 5_000));
        assertTrue(AlmightyTerritory.contains(centerX, centerZ, size, 5_250, 4_800, 5_000));
    }

    @Test
    void excludesOutsideBorderAndPositionsDeeperThanFiveThousand() {
        assertFalse(AlmightyTerritory.contains(0, 0, 10_000, 5_001, 0, 5_000));
        assertFalse(AlmightyTerritory.contains(0, 0, 10_000, 0, 0, 5_000));
        assertEquals(-1.0D, AlmightyTerritory.distanceInsideBorder(0, 0, 10_000, 5_001, 0));
    }

    @Test
    void handlesNonFiniteAndInvalidInputsSafely() {
        assertFalse(AlmightyTerritory.contains(0, 0, 10_000, 0, 0, Double.NaN));
        assertFalse(AlmightyTerritory.contains(0, 0, -1, 0, 0, 5_000));
        assertTrue(Double.isNaN(AlmightyTerritory.distanceInsideBorder(
                0, 0, 10_000, Double.POSITIVE_INFINITY, 0)));
    }

    @Test
    void depthBoundaryIsInclusive() {
        assertTrue(AlmightyTerritory.contains(0, 0, 10_000, 4_999, 0, 5_000));
        assertTrue(AlmightyTerritory.contains(0, 0, 10_000, 5_000, 0, 5_000));
    }
}
