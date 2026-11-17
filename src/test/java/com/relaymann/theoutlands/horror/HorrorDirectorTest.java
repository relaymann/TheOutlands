package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HorrorDirectorTest {
    @Test
    void doesNotProposeEventsInsideTheOpeningThousandBlocks() {
        HorrorDirectorState state = new HorrorDirectorState();

        HorrorDecision decision = HorrorDirector.evaluate(
                new HorrorSnapshot(999.0D, 0.0D, 0),
                state,
                0L,
                0.0D
        );

        assertEquals(HorrorDecision.Reason.OUTSIDE_HORROR_RANGE, decision.reason());
        assertFalse(decision.hasSelectedEvent());
        assertEquals(0.0D, state.getPressure());
    }

    @Test
    void allowsAnInitialBreathingPeriodBeforeTheFirstEncounter() {
        HorrorDirectorState state = new HorrorDirectorState();
        HorrorSnapshot snapshot = new HorrorSnapshot(2_000.0D, 0.69D, 2);

        HorrorDecision first = HorrorDirector.evaluate(snapshot, state, 0L, 0.0D);
        HorrorDecision early = HorrorDirector.evaluate(snapshot, state, 400L, 0.0D);

        assertEquals(HorrorDecision.Reason.BREATHING_ROOM, first.reason());
        assertEquals(HorrorDecision.Reason.BREATHING_ROOM, early.reason());
    }

    @Test
    void proposesAnEligibleEventAfterPressureAndQuietPeriodBuild() {
        HorrorDirectorState state = new HorrorDirectorState();
        HorrorSnapshot snapshot = new HorrorSnapshot(2_000.0D, 0.69D, 2);

        HorrorDirector.evaluate(snapshot, state, 0L, 0.0D);
        HorrorDecision decision = HorrorDirector.evaluate(snapshot, state, 800L, 0.0D);

        assertTrue(decision.hasSelectedEvent());
        assertEquals(HorrorDecision.Reason.EVENT_SELECTED, decision.reason());
        assertTrue(decision.pressure() < state.getPressure() + 4.0D);
    }

    @Test
    void selectedEventCannotImmediatelyRepeatDuringItsCooldown() {
        HorrorDirectorState state = new HorrorDirectorState();
        HorrorSnapshot snapshot = new HorrorSnapshot(2_000.0D, 0.69D, 2);

        HorrorDirector.evaluate(snapshot, state, 0L, 0.0D);
        HorrorDecision selected = HorrorDirector.evaluate(snapshot, state, 800L, 0.0D);
        HorrorDecision next = HorrorDirector.evaluate(snapshot, state, 820L, 0.0D);

        assertTrue(selected.hasSelectedEvent());
        assertEquals(HorrorDecision.Reason.BREATHING_ROOM, next.reason());
    }

    @Test
    void quietPeriodShortensWithDistanceButNeverBecomesConstantCombat() {
        assertTrue(HorrorDirector.minimumQuietPeriod(8) < HorrorDirector.minimumQuietPeriod(2));
        assertEquals(HorrorDirector.MINIMUM_QUIET_TICKS, HorrorDirector.minimumQuietPeriod(100));
        assertTrue(HorrorDirector.minimumQuietPeriod(100) >= 240L);
    }

    @Test
    void invalidRandomRollIsHandledSafely() {
        HorrorDirectorState state = new HorrorDirectorState();
        HorrorSnapshot snapshot = new HorrorSnapshot(2_000.0D, 0.69D, 2);

        HorrorDirector.evaluate(snapshot, state, 0L, Double.NaN);
        HorrorDecision decision = HorrorDirector.evaluate(snapshot, state, 800L, Double.NaN);

        assertTrue(decision.hasSelectedEvent());
    }
}
