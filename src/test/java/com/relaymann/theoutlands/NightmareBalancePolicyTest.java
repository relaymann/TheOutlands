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
    void encounterBudgetsRiseMonotonically() {
        assertTrue(NightmareBalancePolicy.entityCap(0L, 25_000.0D)
                > NightmareBalancePolicy.entityCap(0L, 0.0D));
        assertTrue(NightmareBalancePolicy.threatBudget(0L, 25_000.0D)
                > NightmareBalancePolicy.threatBudget(0L, 0.0D));
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
