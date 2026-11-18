package com.relaymann.theoutlands.horror;

import java.util.ArrayList;
import java.util.List;

/** Pacing and weighted event selection from distance, pressure, and local context. */
public final class HorrorDirector {
    public static final double EVENT_PRESSURE_THRESHOLD = 10.0D;
    public static final long MINIMUM_QUIET_TICKS = 240L, MAXIMUM_QUIET_TICKS = 730L;
    private static final List<HorrorEventDefinition> EVENTS = List.of(
            new HorrorEventDefinition("ambient_displacement", 1_000, 10, 8, 1_200, 3),
            new HorrorEventDefinition("false_footsteps", 1_500, 11, 6, 1_600, 4),
            new HorrorEventDefinition("distant_figure", 2_500, 12, 5, 2_400, 5),
            new HorrorEventDefinition("unexplained_message", 1_500, 12, 4, 2_400, 4),
            new HorrorEventDefinition("environmental_distortion", 5_000, 13, 4, 3_000, 6),
            new HorrorEventDefinition("hostile_warning", 8_000, 14, 3, 3_600, 7),
            new HorrorEventDefinition("false_death", 12_000, 17, 1, 7_200, 10),
            new HorrorEventDefinition("the_return", 8_000, 15, 2, 5_000, 8),
            new HorrorEventDefinition("direct_intervention", 15_000, 16, 1, 6_000, 10));
    private HorrorDirector() {}
    public static List<HorrorEventDefinition> getEventDefinitions() { return EVENTS; }

    public static HorrorDecision evaluate(HorrorSnapshot snapshot, HorrorDirectorState state, long time, double roll) {
        return evaluate(snapshot, state, HorrorContext.neutral(), time, roll, HorrorSettings.DEFAULT);
    }

    public static HorrorDecision evaluate(HorrorSnapshot snapshot, HorrorDirectorState state,
                                          HorrorContext context, long time, double roll) {
        return evaluate(snapshot, state, context, time, roll, HorrorSettings.DEFAULT);
    }

    public static HorrorDecision evaluate(HorrorSnapshot snapshot, HorrorDirectorState state,
                                          HorrorContext context, long time, double roll,
                                          HorrorSettings settings) {
        if (snapshot == null || state == null || context == null || settings == null)
            throw new IllegalArgumentException("Missing director input");
        double distance = snapshot.distanceFromSpawn();
        if (!Double.isFinite(distance) || snapshot.escalationValue() <= 0.0D) {
            updatePressure(state, time, 0, false, settings.intensity());
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.OUTSIDE_HORROR_RANGE, state.pressure());
        }
        if (state.lastEncounterGameTime() == Long.MIN_VALUE) state.setLastEncounterGameTime(time);
        updatePressure(state, time, snapshot.escalationValue(), true, settings.intensity());
        if (time - state.lastEncounterGameTime() < minimumQuietPeriod(snapshot.escalationTier()))
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.BREATHING_ROOM, state.pressure());
        if (!settings.eventsEnabled())
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.NO_ELIGIBLE_EVENT, state.pressure());
        if (state.pressure() < EVENT_PRESSURE_THRESHOLD)
            return HorrorDecision.withoutEvent(HorrorDecision.Reason.BUILDING_PRESSURE, state.pressure());

        List<Weighted> eligible = new ArrayList<>();
        double total = 0;
        for (HorrorEventDefinition event : EVENTS) {
            if (distance < event.minimumDistance() || state.pressure() < event.minimumPressure()
                    || state.isCoolingDown(event.id(), time) || state.wasRecentlySelected(event.id())) continue;
            if ("the_return".equals(event.id()) && !context.hasSeenNull()) continue;
            if ("false_death".equals(event.id()) && context.previousEncounterCount() < 2) continue;
            double weight = event.weight() * context.weightMultiplier(event.id()) * settings.eventFrequency();
            if (weight > 0 && Double.isFinite(weight)) { eligible.add(new Weighted(event, weight)); total += weight; }
        }
        if (eligible.isEmpty()) return HorrorDecision.withoutEvent(HorrorDecision.Reason.NO_ELIGIBLE_EVENT, state.pressure());
        double normalized = Double.isFinite(roll) ? Math.max(0, Math.min(Math.nextDown(1.0), roll)) : 0;
        double pick = normalized * total;
        HorrorEventDefinition selected = eligible.get(eligible.size() - 1).event();
        for (Weighted candidate : eligible) {
            pick -= candidate.weight();
            if (pick < 0) { selected = candidate.event(); break; }
        }
        state.setLastEncounterGameTime(time);
        state.startCooldown(selected.id(), safeAdd(time, selected.cooldownTicks()));
        state.remember(selected.id());
        state.setPressure(state.pressure() - selected.pressureCost());
        return new HorrorDecision(HorrorDecision.Reason.EVENT_SELECTED, selected.id(), state.pressure());
    }

    public static long minimumQuietPeriod(int tier) {
        return Math.max(MINIMUM_QUIET_TICKS, MAXIMUM_QUIET_TICKS - (long) Math.max(0, tier) * 70L);
    }

    private static void updatePressure(HorrorDirectorState state, long time, double escalation,
                                       boolean active, double intensity) {
        long previous = state.lastEvaluationGameTime();
        long ticks = previous == Long.MIN_VALUE ? 20 : Math.max(0, Math.min(1_200, time - previous));
        state.setLastEvaluationGameTime(time);
        double seconds = ticks / 20.0D;
        if (!active) { state.setPressure(state.pressure() - seconds * 0.5D); return; }
        double distanceGain = Math.min(0.45D, Math.log1p(Math.max(0, escalation)) * 0.06D);
        state.setPressure(state.pressure() + (0.35D + distanceGain) * seconds * intensity);
    }

    private static long safeAdd(long a, long b) { return b > 0 && a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b; }
    private record Weighted(HorrorEventDefinition event, double weight) {}
}
