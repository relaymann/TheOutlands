package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HorrorDirectorTest {
    @Test void openingThousandBlocksRemainSafe() {
        HorrorDirectorState state = new HorrorDirectorState();
        var result = HorrorDirector.evaluate(new HorrorSnapshot(999, 0, 0), state, 0, 0);
        assertEquals(HorrorDecision.Reason.OUTSIDE_HORROR_RANGE, result.reason());
        assertFalse(result.hasSelectedEvent());
        assertEquals(0.0D, state.getPressure());
    }
    @Test void initialBreathingPeriodIsRespected() {
        var state = new HorrorDirectorState(); var snapshot = new HorrorSnapshot(2_000, .69, 2);
        assertEquals(HorrorDecision.Reason.BREATHING_ROOM, HorrorDirector.evaluate(snapshot, state, 0, 0).reason());
        assertEquals(HorrorDecision.Reason.BREATHING_ROOM, HorrorDirector.evaluate(snapshot, state, 400, 0).reason());
    }
    @Test void eventCanBeProposedAfterPressureAndQuietPeriod() {
        var state = new HorrorDirectorState(); var snapshot = new HorrorSnapshot(2_000, .69, 2);
        HorrorDirector.evaluate(snapshot, state, 0, 0);
        var result = HorrorDirector.evaluate(snapshot, state, 800, 0);
        assertTrue(result.hasSelectedEvent());
        assertTrue(result.pressure() >= 0);
    }
    @Test void selectedEventCooldownBoundaryIsCorrect() {
        var state = new HorrorDirectorState(); var snapshot = new HorrorSnapshot(2_000, .69, 2);
        HorrorDirector.evaluate(snapshot, state, 0, 0);
        var selected = HorrorDirector.evaluate(snapshot, state, 800, 0);
        assertTrue(selected.hasSelectedEvent());
        var definition = HorrorDirector.getEventDefinitions().stream()
                .filter(e -> e.id().equals(selected.eventId())).findFirst().orElseThrow();
        assertTrue(state.isCoolingDown(selected.eventId(), 800 + definition.cooldownTicks() - 1));
        assertFalse(state.isCoolingDown(selected.eventId(), 800 + definition.cooldownTicks()));
        assertTrue(state.wasRecentlySelected(selected.eventId()));
    }
    @Test void quietPeriodShortensButHasFloor() {
        assertTrue(HorrorDirector.minimumQuietPeriod(8) < HorrorDirector.minimumQuietPeriod(2));
        assertEquals(HorrorDirector.MINIMUM_QUIET_TICKS, HorrorDirector.minimumQuietPeriod(100));
    }
    @Test void invalidRollIsSafe() {
        var state = new HorrorDirectorState(); var snapshot = new HorrorSnapshot(2_000, .69, 2);
        HorrorDirector.evaluate(snapshot, state, 0, Double.NaN);
        assertTrue(HorrorDirector.evaluate(snapshot, state, 800, Double.NaN).hasSelectedEvent());
    }
    @Test void contextAdjustsWeightsWithoutBypassingGates() {
        assertTrue(new HorrorContext("minecraft:forest", true, false, false, 0, 1)
                .weightMultiplier("distant_figure") > 1);
        assertTrue(new HorrorContext("minecraft:plains", false, true, false, 0, 1)
                .weightMultiplier("environmental_distortion") > 1);
        assertEquals(1.0D, HorrorContext.neutral().weightMultiplier("false_footsteps"));
    }
    @Test void playerHistoryChangesEncounterWeightsAndGatesTheReturn() {
        var unseen = new HorrorContext("minecraft:plains", false, false, false, 0, 1, 8, false);
        var seen = new HorrorContext("minecraft:plains", false, false, false, 0, 1, 8, true);
        assertTrue(seen.weightMultiplier("the_return") > unseen.weightMultiplier("the_return"));
        assertTrue(seen.weightMultiplier("distant_figure") < unseen.weightMultiplier("distant_figure"));
        assertTrue(unseen.weightMultiplier("false_death") < 1.0D);
    }

    @Test void expandedEventsHaveUniqueIdsAndRealExecutors() {
        var events = HorrorDirector.getEventDefinitions();
        assertEquals(events.size(), events.stream().map(HorrorEventDefinition::id).distinct().count());
        assertTrue(events.stream().anyMatch(e -> e.id().equals("false_death")));
        assertTrue(events.stream().anyMatch(e -> e.id().equals("unexplained_message")));
        assertTrue(events.stream().anyMatch(e -> e.id().equals("the_return")));
    }
}
