package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AggressiveJumpscarePolicyTest {
    @Test void nothingCanJumpscareBeforeElevenThousandBlocks() {
        assertFalse(AggressiveJumpscarePolicy.isUnlocked(10_999.99D));
        assertEquals(0.0D, AggressiveJumpscarePolicy.chancePerEvaluation(10_999.99D));
    }

    @Test void unlockDoesNotMeanAnImmediateScare() {
        assertTrue(AggressiveJumpscarePolicy.isUnlocked(11_000.0D));
        assertFalse(AggressiveJumpscarePolicy.shouldAttempt(11_000.0D,
                true, false, false, 36_000L, 0.5D));
    }

    @Test void requiresAnIntroducedAggressiveEntityAndCooldown() {
        assertFalse(AggressiveJumpscarePolicy.shouldAttempt(30_000.0D,
                false, false, false, 100_000L, 0.0D));
        assertFalse(AggressiveJumpscarePolicy.shouldAttempt(30_000.0D,
                true, false, false, 35_999L, 0.0D));
        assertFalse(AggressiveJumpscarePolicy.shouldAttempt(30_000.0D,
                true, false, true, 100_000L, 0.0D));
    }

    @Test void probabilityRisesSlowlyAndHasALowHardCap() {
        double atUnlock = AggressiveJumpscarePolicy.chancePerEvaluation(11_000.0D);
        double atMidpoint = AggressiveJumpscarePolicy.chancePerEvaluation(55_500.0D);
        double atExtremeDistance = AggressiveJumpscarePolicy.chancePerEvaluation(1_000_000.0D);
        assertEquals(0.00004D, atUnlock, 0.0000001D);
        assertTrue(atMidpoint > atUnlock);
        assertTrue(atExtremeDistance > atMidpoint);
        assertEquals(0.00015D, atExtremeDistance, 0.0000001D);
    }
}
