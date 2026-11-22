package com.relaymann.theoutlands;

/** Pure NIGHTMARE pressure curves shared by spawning and the encounter director. */
public final class NightmareBalancePolicy {
    private NightmareBalancePolicy() {}

    /** Progression combines elapsed world days with how far the player has travelled. */
    public static int pressureTier(long dayTime, double distanceFromSpawn) {
        long days = Math.max(0L, dayTime) / 24_000L;
        int ageTier = days >= 56 ? 5 : days >= 28 ? 4 : days >= 14 ? 3
                : days >= 7 ? 2 : days >= 3 ? 1 : 0;
        int distanceTier = distanceFromSpawn >= 25_000.0D ? 5
                : distanceFromSpawn >= 14_000.0D ? 4
                : distanceFromSpawn >= 7_000.0D ? 3
                : distanceFromSpawn >= 2_500.0D ? 2
                : distanceFromSpawn >= 750.0D ? 1 : 0;
        return Math.max(ageTier, distanceTier);
    }

    public static int entityCap(long dayTime, double distanceFromSpawn) {
        return 16 + pressureTier(dayTime, distanceFromSpawn) * 4;
    }

    public static int threatBudget(long dayTime, double distanceFromSpawn) {
        return 28 + pressureTier(dayTime, distanceFromSpawn) * 9;
    }

    /** Scale event pacing by the configured frequency while keeping invalid values safe. */
    public static int scaleEventDelayTicks(int baseDelayTicks, double frequencyMultiplier) {
        if (baseDelayTicks <= 0) return 1;
        double frequency = Double.isFinite(frequencyMultiplier)
                ? Math.max(0.1D, Math.min(3.0D, frequencyMultiplier)) : 1.0D;
        return Math.max(1, (int) Math.round(baseDelayTicks / frequency));
    }

    /** A watcher freezes only when it is visible and the player is actually looking at it. */
    public static boolean watcherShouldFreeze(double distanceSquared, double lookDot, boolean lineOfSight) {
        return lineOfSight && Double.isFinite(distanceSquared) && Double.isFinite(lookDot)
                && distanceSquared >= 0.0D && distanceSquared <= 1_600.0D && lookDot >= 0.78D;
    }

    /** Deterministic range selection; randomBits should come from the player's RNG. */
    public static int nextEventDelayTicks(long dayTime, double distanceFromSpawn, long randomBits) {
        int tier = pressureTier(dayTime, distanceFromSpawn);
        int minimum = Math.max(600, 1_500 - tier * 170);
        int spread = Math.max(720, 2_100 - tier * 240);
        return minimum + (int) Long.remainderUnsigned(randomBits, spread);
    }
}
