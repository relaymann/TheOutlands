package com.relaymann.theoutlands.horror;

import java.util.ArrayList;
import java.util.List;

/**
 * Controls encounter pacing and proposes event families from player distance and pressure.
 *
 * <p>The director intentionally does not spawn entities, change blocks, or send client
 * effects. Those actions will be handled by dedicated event executors. Keeping selection
 * separate makes pacing testable and prevents the player tick hook from becoming expensive.</p>
 */
public final class HorrorDirector {
    public static final double EVENT_PRESSURE_THRESHOLD = 10.0D;
    public static final long MINIMUM_QUIET_TICKS = 240L;
    public static final long MAXIMUM_QUIET_TICKS = 730L;

    private static final double MAX_ELAPSED_SECONDS_PER_EVALUATION = 60.0D;
    private static final double BASE_PRESSURE_PER_SECOND = 0.35D;
    private static final double DISTANCE_PRESSURE_FACTOR = 0.06D;

    private static final List<HorrorEventDefinition> EVENTS = List.of(
            new HorrorEventDefinition("ambient_displacement", 1_000.0D, 10.0D, 8, 1_200L, 3.0D),
            new HorrorEventDefinition("false_footsteps", 1_500.0D, 11.0D, 6, 1_600L, 4.0D),
            new HorrorEventDefinition("distant_figure", 2_500.0D, 12.0D, 5, 2_400L, 5.0D),
            new HorrorEventDefinition("environmental_distortion", 5_000.0D, 13.0D, 4, 3_000L, 6.0D),
            new HorrorEventDefinition("hostile_warning", 8_000.0D, 14.0D, 3, 3_600L, 7.0D),
            new HorrorEventDefinition("direct_intervention", 15_000.0D, 16.0D, 1, 6_000L, 10.0D)
    );

    private HorrorDirector() {
    }

    public static List<HorrorEventDefinition> getEventDefinitions() {
        return EVENTS;
    }

    /**
     * Evaluate once per scheduler interval. selectionRoll must be in [0, 1); callers
     * normally provide a value from the server-side player's random source.
     */
    public static HorrorDecision evaluate(
            HorrorSnapshot snapshot,
            HorrorDirectorState state,
            long gameTime,
            double selectionRoll
    ) {
        if (snapshot == null || state == null) {
            throw new IllegalArgumentException("Snapshot and state are required");
        }

        double distance = snapshot.distanceFromSpawn();
        if (!Double.isFinite(distance) || distance <= DistanceProgression.FIRST_ANOMALY_DISTANCE) {
            updatePressure(state, gameTime, 0.0D, false);
            return HorrorDecision.withoutEvent(
                    HorrorDecision.Reason.OUTSIDE_HORROR_RANGE, state.pressure());
        }

        if (state.lastEncounterGameTime() == Long.MIN_VALUE) {
            state.setLastEncounterGameTime(gameTime);
        }

        updatePressure(state, gameTime, snapshot.escalationValue(), true);

        long quietPeriod = minimumQuietPeriod(snapshot.escalationTier());
        if (gameTime - state.lastEncounterGameTime() < quietPeriod) {
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.BREATHING_ROOM, state.pressure());
        }

        if (state.pressure() < EVENT_PRESSURE_THRESHOLD) {
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.BUILDING_PRESSURE, state.pressure());
        }

        List<HorrorEventDefinition> eligible = new ArrayList<>();
        int totalWeight = 0;
        for (HorrorEventDefinition event : EVENTS) {
            if (distance < event.minimumDistance()
                    || state.pressure() < event.minimumPressure()
                    || state.isCoolingDown(event.id(), gameTime)
                    || state.wasRecentlySelected(event.id())) {
                continue;
            }

            eligible.add(event);
            totalWeight += event.weight();
        }

        if (eligible.isEmpty()) {
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.NO_ELIGIBLE_EVENT, state.pressure());
        }

        double normalizedRoll = Double.isFinite(selectionRoll)
                ? Math.max(0.0D, Math.min(Math.nextDown(1.0D), selectionRoll))
                : 0.0D;
        int selectedWeight = (int) (normalizedRoll * totalWeight);
        HorrorEventDefinition selected = eligible.get(eligible.size() - 1);
        for (HorrorEventDefinition event : eligible) {
            selectedWeight -= event.weight();
            if (selectedWeight < 0) {
                selected = event;
                break;
            }
        }

        state.setLastEncounterGameTime(gameTime);
        state.startCooldown(selected.id(), safeAdd(gameTime, selected.cooldownTicks()));
        state.remember(selected.id());
        state.setPressure(state.pressure() - selected.pressureCost());

        return new HorrorDecision(HorrorDecision.Reason.EVENT_SELECTED, selected.id(), state.pressure());
    }

    /**
     * Quiet periods shorten as the player travels farther, but never below 12 seconds.
     */
    public static long minimumQuietPeriod(int escalationTier) {
        int nonNegativeTier = Math.max(0, escalationTier);
        return Math.max(MINIMUM_QUIET_TICKS, MAXIMUM_QUIET_TICKS - (long) nonNegativeTier * 70L);
    }

    private static void updatePressure(
            HorrorDirectorState state,
            long gameTime,
            double escalationValue,
            boolean insideHorrorRange
    ) {
        long previousTime = state.lastEvaluationGameTime();
        long elapsedTicks = previousTime == Long.MIN_VALUE
                ? 20L
                : Math.max(0L, Math.min((long) (MAX_ELAPSED_SECONDS_PER_EVALUATION * 20.0D), gameTime - previousTime));
        state.setLastEvaluationGameTime(gameTime);

        double elapsedSeconds = elapsedTicks / 20.0D;
        if (!insideHorrorRange) {
            state.setPressure(state.pressure() - elapsedSeconds * 0.5D);
            return;
        }

        double distanceContribution = Math.min(0.45D, Math.log1p(Math.max(0.0D, escalationValue)) * DISTANCE_PRESSURE_FACTOR);
        double pressureGain = (BASE_PRESSURE_PER_SECOND + distanceContribution) * elapsedSeconds;
        state.setPressure(state.pressure() + pressureGain);
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
