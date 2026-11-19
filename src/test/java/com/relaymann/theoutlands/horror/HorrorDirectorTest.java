package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.stream.Collectors;
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
        var unseen = new HorrorContext("minecraft:plains", false, false, false, 0, 1, 1, false);
        var seen = new HorrorContext("minecraft:plains", false, false, false, 0, 1, 8, true);
        assertTrue(seen.weightMultiplier("the_return") > unseen.weightMultiplier("the_return"));
        assertTrue(seen.weightMultiplier("distant_figure") < unseen.weightMultiplier("distant_figure"));
        assertTrue(unseen.weightMultiplier("false_death") < 1.0D);
    }
    @Test void almightyInfluenceRequiresBorderTerritoryContext() {
        var snapshot = new HorrorSnapshot(50_000, 3.0, 6);
        var outside = new HorrorDirectorState();
        outside.setPressure(50.0D);
        outside.setLastEvaluationGameTime(0);
        outside.setLastEncounterGameTime(0);
        var outsideResult = HorrorDirector.evaluate(snapshot, outside, new HorrorContext("minecraft:plains", false, false, false, 0, 1), 800, Math.nextDown(1.0));
        assertNotEquals("almighty_influence", outsideResult.eventId());

        var context = new HorrorContext("minecraft:plains", false, false, false, 0, 1, 0, false, false, 0, 0, true);
        boolean selectedInsideTerritory = false;
        // Weighted selection does not promise the last event is selected at a near-1 roll.
        // Sweep deterministic rolls to test the territory gate rather than ordering/weight assumptions.
        for (int i = 0; i < 10_000 && !selectedInsideTerritory; i++) {
            var inside = new HorrorDirectorState();
            inside.setPressure(50.0D);
            inside.setLastEvaluationGameTime(0);
            inside.setLastEncounterGameTime(0);
            var insideResult = HorrorDirector.evaluate(snapshot, inside, context, 800, i / 10_000.0D);
            selectedInsideTerritory = "almighty_influence".equals(insideResult.eventId());
        }
        assertTrue(selectedInsideTerritory, "Almighty influence should become eligible inside border territory");
    }

    @Test void settingsCanTuneOrDisableDirectorWithoutLoadingNeoForgeConfig() {
        var state = new HorrorDirectorState();
        var snapshot = new HorrorSnapshot(2_000, .69, 2);
        var disabled = new HorrorSettings(1.0D, 1.0D, false);
        HorrorDirector.evaluate(snapshot, state, HorrorContext.neutral(), 0, 0, disabled);
        var result = HorrorDirector.evaluate(snapshot, state, HorrorContext.neutral(), 800, 0, disabled);
        assertEquals(HorrorDecision.Reason.NO_ELIGIBLE_EVENT, result.reason());
    }
    @Test void expandedEventsHaveUniqueIdsAndConcreteExecutionCoverage() {
        var ids = HorrorDirector.getEventDefinitions().stream().map(HorrorEventDefinition::id).collect(Collectors.toSet());
        assertEquals(HorrorDirector.getEventDefinitions().size(), ids.size());
        assertEquals(ids, HorrorEventCatalog.ids());
        assertTrue(ids.containsAll(Set.of("false_death", "unexplained_message", "the_return", "familiar_echo", "environmental_hush", "almighty_influence",
                "false_player", "wrong_sounds", "missing_terrain", "observer", "fake_structure", "fear_whisper", "fear_presence", "fear_call",
                "repeated_location", "silent_world", "entity_imitation", "distant_scream")));
    }
}
