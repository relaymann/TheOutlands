package com.relaymann.theoutlands.horror;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class HorrorDirectorState {
    static final int RECENT_EVENT_MEMORY = 3;

    private double pressure;
    private long lastEvaluationGameTime = Long.MIN_VALUE;
    private long lastEncounterGameTime = Long.MIN_VALUE;
    private final Map<String, Long> cooldownUntil = new HashMap<>();
    private final Deque<String> recentEvents = new ArrayDeque<>();

    double pressure() {
        return pressure;
    }

    void setPressure(double pressure) {
        this.pressure = Math.max(0.0D, Math.min(100.0D, pressure));
    }

    long lastEvaluationGameTime() {
        return lastEvaluationGameTime;
    }

    void setLastEvaluationGameTime(long gameTime) {
        this.lastEvaluationGameTime = gameTime;
    }

    long lastEncounterGameTime() {
        return lastEncounterGameTime;
    }

    void setLastEncounterGameTime(long gameTime) {
        this.lastEncounterGameTime = gameTime;
    }

    boolean isCoolingDown(String eventId, long gameTime) {
        return cooldownUntil.getOrDefault(eventId, Long.MIN_VALUE) > gameTime;
    }

    void startCooldown(String eventId, long untilGameTime) {
        cooldownUntil.put(eventId, untilGameTime);
    }

    boolean wasRecentlySelected(String eventId) {
        return recentEvents.contains(eventId);
    }

    void remember(String eventId) {
        recentEvents.remove(eventId);
        recentEvents.addFirst(eventId);
        while (recentEvents.size() > RECENT_EVENT_MEMORY) {
            recentEvents.removeLast();
        }
    }

    public double getPressure() {
        return pressure;
    }

    public void reset() {
        pressure = 0.0D;
        lastEvaluationGameTime = Long.MIN_VALUE;
        lastEncounterGameTime = Long.MIN_VALUE;
        cooldownUntil.clear();
        recentEvents.clear();
    }
}
