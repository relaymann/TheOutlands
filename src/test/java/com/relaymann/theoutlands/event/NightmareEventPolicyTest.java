package com.relaymann.theoutlands.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NightmareEventPolicyTest {
    @Test void adventureModeRemainsAllowed() {
        assertFalse(NightmareEventPolicy.shouldBlockGameModeChange(true, true, false, false));
    }

    @Test void creativeAndSpectatorRemainBlockedInNightmare() {
        assertTrue(NightmareEventPolicy.shouldBlockGameModeChange(true, true, true, false));
        assertTrue(NightmareEventPolicy.shouldBlockGameModeChange(true, true, false, true));
    }

    @Test void nonNightmareAndDeadPlayersAreNotRestricted() {
        assertFalse(NightmareEventPolicy.shouldBlockGameModeChange(false, true, true, false));
        assertFalse(NightmareEventPolicy.shouldBlockGameModeChange(true, false, true, false));
    }

    @Test void bloodMoonStagesAdvanceAtTheirOwnThresholds() {
        assertEquals(0, NightmareEventPolicy.nextBloodMoonStage(0, 599));
        assertEquals(1, NightmareEventPolicy.nextBloodMoonStage(0, 600));
        assertEquals(2, NightmareEventPolicy.nextBloodMoonStage(1, 1_200));
        assertEquals(3, NightmareEventPolicy.nextBloodMoonStage(2, 1_800));
    }

    @Test void missedBloodMoonStagesCatchUpOneAtATime() {
        assertEquals(1, NightmareEventPolicy.nextBloodMoonStage(0, 2_000));
        assertEquals(2, NightmareEventPolicy.nextBloodMoonStage(1, 2_000));
        assertEquals(3, NightmareEventPolicy.nextBloodMoonStage(2, 2_000));
    }

    @Test void bloodMoonStageNeverExceedsThreeOrMovesBackward() {
        assertEquals(3, NightmareEventPolicy.nextBloodMoonStage(3, 99_999));
        assertEquals(2, NightmareEventPolicy.nextBloodMoonStage(2, 1_000));
        assertEquals(0, NightmareEventPolicy.nextBloodMoonStage(-4, 0));
    }
}
