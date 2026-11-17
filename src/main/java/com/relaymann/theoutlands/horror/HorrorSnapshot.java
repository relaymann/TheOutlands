package com.relaymann.theoutlands.horror;

/**
 * Lightweight derived state for a player at a particular evaluation time.
 *
 * <p>This is intentionally not persistent data. Distance and intensity can be recomputed
 * after login; future story flags, evidence, and lasting consequences will use separate
 * persistent player/world data.</p>
 */
public record HorrorSnapshot(double distanceFromSpawn, double escalationValue, int escalationTier) {
    public boolean hasEnteredTheOutlands() {
        return distanceFromSpawn > DistanceProgression.FIRST_ANOMALY_DISTANCE;
    }
}
