package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApostleProgressionTest {
    @Test
    void nullRemainsEligibleAtEveryDistance() {
        assertTrue(ApostleProgression.evaluate(0, false).nullMayAppear());
        assertTrue(ApostleProgression.evaluate(9_000_000, false).nullMayAppear());
    }

    @Test
    void fearIntroductionUnlocksAtFifteenThousand() {
        assertFalse(ApostleProgression.evaluate(14_999, false).fearIntroductionUnlocked());
        assertTrue(ApostleProgression.evaluate(15_000, false).fearIntroductionUnlocked());
    }

    @Test
    void deathIntroductionUnlocksAtFiftyThousand() {
        assertFalse(ApostleProgression.evaluate(49_999, false).deathIntroductionUnlocked());
        assertTrue(ApostleProgression.evaluate(50_000, false).deathIntroductionUnlocked());
        assertTrue(ApostleProgression.evaluate(50_000, false).deathTerritoryActive());
    }

    @Test
    void fearFullTerritoryMilestoneUnlocksAtOneHundredThousand() {
        assertFalse(ApostleProgression.evaluate(99_999, false).fearFullTerritoryUnlocked());
        assertTrue(ApostleProgression.evaluate(100_000, false).fearFullTerritoryUnlocked());
    }

    @Test
    void almightyTerritoryOverridesDeathTerritory() {
        var progression = ApostleProgression.evaluate(500_000, true);
        assertTrue(progression.almightyTerritoryActive());
        assertFalse(progression.deathTerritoryActive());
    }

    @Test
    void invalidDistanceDoesNotUnlockLaterApostles() {
        var progression = ApostleProgression.evaluate(Double.NaN, false);
        assertFalse(progression.fearIntroductionUnlocked());
        assertFalse(progression.deathIntroductionUnlocked());
    }
}
