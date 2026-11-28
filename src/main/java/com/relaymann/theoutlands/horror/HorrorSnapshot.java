package com.relaymann.theoutlands.horror;

public record HorrorSnapshot(double distanceFromSpawn, double escalationValue, int escalationTier) {
    public boolean hasEnteredTheOutlands() {
        return distanceFromSpawn > DistanceProgression.FIRST_ANOMALY_DISTANCE;
    }
}
