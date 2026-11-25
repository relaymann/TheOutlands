package com.relaymann.theoutlands.block;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SurvivalDefensePolicyTest {
    @Test void firstContactAlwaysTriggersRegardlessOfEntityAge() {
        assertTrue(SurvivalDefensePolicy.isReady(false, 19L, 0L, 20L));
        assertTrue(SurvivalDefensePolicy.isReady(false, 0L, 0L, 20L));
    }

    @Test void repeatContactWaitsForTheConfiguredCooldown() {
        assertFalse(SurvivalDefensePolicy.isReady(true, 119L, 100L, 20L));
        assertTrue(SurvivalDefensePolicy.isReady(true, 120L, 100L, 20L));
    }

    @Test void aWorldTimeResetDoesNotLockTheTrapForever() {
        assertTrue(SurvivalDefensePolicy.isReady(true, 4L, 900L, 20L));
    }

    @Test void nonPositiveCooldownIsAlwaysReady() {
        assertTrue(SurvivalDefensePolicy.isReady(true, 100L, 100L, 0L));
        assertTrue(SurvivalDefensePolicy.isReady(true, 100L, 100L, -1L));
    }
}
