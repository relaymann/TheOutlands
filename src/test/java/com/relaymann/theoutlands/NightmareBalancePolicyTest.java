package com.relaymann.theoutlands;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NightmareBalancePolicyTest {
    @Test
    void pressureEscalatesWithWorldAgeAndDistance() {
        assertEquals(0, NightmareBalancePolicy.pressureTier(0L, 0.0D));
        assertEquals(1, NightmareBalancePolicy.pressureTier(0L, 1_000.0D));
        assertEquals(3, NightmareBalancePolicy.pressureTier(0L, 8_000.0D));
        assertEquals(4, NightmareBalancePolicy.pressureTier(30L * 24_000L, 0.0D));
    }

    @Test
    void nightmareNaturalSpawnsKeepAnOpeningBufferWithoutErasingTerritoryProgression() {
        assertEquals(900.0D, NightmareBalancePolicy.nightmareMinimumSpawnDistance(250.0D));
        assertEquals(1_275.0D, NightmareBalancePolicy.nightmareMinimumSpawnDistance(1_500.0D));
        assertEquals(12_750.0D, NightmareBalancePolicy.nightmareMinimumSpawnDistance(15_000.0D));
        assertTrue(NightmareBalancePolicy.canStartNightmareSpawnEvent(900.0D));
        assertFalse(NightmareBalancePolicy.canStartNightmareSpawnEvent(899.9D));
        assertFalse(NightmareBalancePolicy.canStartNightmareSpawnEvent(Double.NaN));
    }

    @Test
    void encounterBudgetsRiseMonotonically() {
        assertTrue(NightmareBalancePolicy.entityCap(0L, 25_000.0D)
                > NightmareBalancePolicy.entityCap(0L, 0.0D));
        assertTrue(NightmareBalancePolicy.threatBudget(0L, 25_000.0D)
                > NightmareBalancePolicy.threatBudget(0L, 0.0D));
    }

    @Test
    void configuredEventFrequencyScalesDelaySafely() {
        int base = 1_200;
        assertEquals(2_400, NightmareBalancePolicy.scaleEventDelayTicks(base, 0.5D));
        assertEquals(1_200, NightmareBalancePolicy.scaleEventDelayTicks(base, 1.0D));
        assertEquals(400, NightmareBalancePolicy.scaleEventDelayTicks(base, 3.0D));
        assertEquals(12_000, NightmareBalancePolicy.scaleEventDelayTicks(base, 0.0D));
        assertEquals(1_200, NightmareBalancePolicy.scaleEventDelayTicks(base, Double.NaN));
    }

    @Test
    void packCoordinationKeepsValidTargetsInMultiplayer() {
        assertTrue(NightmareBalancePolicy.shouldReplaceTarget(false, false, false, true, false, false));
        assertTrue(NightmareBalancePolicy.shouldReplaceTarget(true, false, true, true, false, false));
        assertTrue(NightmareBalancePolicy.shouldReplaceTarget(true, true, true, true, true, false));
        assertTrue(NightmareBalancePolicy.shouldReplaceTarget(true, true, true, false, false, false));
        assertTrue(NightmareBalancePolicy.shouldReplaceTarget(true, true, true, true, false, true));
        assertFalse(NightmareBalancePolicy.shouldReplaceTarget(true, true, true, true, false, false));
        assertFalse(NightmareBalancePolicy.shouldReplaceTarget(true, true, false, true, false, false));
    }

    @Test
    void watcherOnlyFreezesWhenClearlyObserved() {
        assertTrue(NightmareBalancePolicy.watcherShouldFreeze(100.0D, 0.9D, true));
        assertFalse(NightmareBalancePolicy.watcherShouldFreeze(100.0D, 0.9D, false));
        assertFalse(NightmareBalancePolicy.watcherShouldFreeze(100.0D, 0.2D, true));
        assertFalse(NightmareBalancePolicy.watcherShouldFreeze(1_700.0D, 1.0D, true));
    }

    @Test
    void eventDelayStaysInsideProgressionBand() {
        for (int tier = 0; tier <= 5; tier++) {
            long age = tier == 0 ? 0L : (long) Math.pow(2, tier + 1) * 24_000L;
            int delay = NightmareBalancePolicy.nextEventDelayTicks(age, 0.0D, 123456789L);
            assertTrue(delay >= 600);
            assertTrue(delay <= 3_600);
        }
    }
}
