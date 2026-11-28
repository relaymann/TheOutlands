package com.relaymann.theoutlands;

public final class NightmareBalancePolicy {
    private NightmareBalancePolicy() {}

    public static int pressureTier(long dayTime, double distanceFromSpawn) {
        long days = Math.max(0L, dayTime) / 24_000L;
        int ageTier = days >= 56 ? 5 : days >= 28 ? 4 : days >= 14 ? 3
                : days >= 7 ? 2 : days >= 3 ? 1 : 0;
        double safeDistance = Double.isFinite(distanceFromSpawn) ? Math.max(0.0D, distanceFromSpawn) : 0.0D;
        int distanceTier = safeDistance >= 25_000.0D ? 5
                : safeDistance >= 14_000.0D ? 4
                : safeDistance >= 7_000.0D ? 3
                : safeDistance >= 2_500.0D ? 2
                : safeDistance >= 750.0D ? 1 : 0;
        return Math.max(ageTier, distanceTier);
    }

    public static double nightmareMinimumSpawnDistance(double normalMinimumDistance) {
        double safeMinimum = Double.isFinite(normalMinimumDistance)
                ? Math.max(0.0D, normalMinimumDistance) : 0.0D;
        return Math.max(900.0D, safeMinimum * 0.85D);
    }

    public static boolean canStartNightmareSpawnEvent(double distanceFromSpawn) {
        return Double.isFinite(distanceFromSpawn) && distanceFromSpawn >= 900.0D;
    }

    public static int entityCap(long dayTime, double distanceFromSpawn) {
        return 12 + pressureTier(dayTime, distanceFromSpawn) * 3;
    }

    public static int threatBudget(long dayTime, double distanceFromSpawn) {
        return 24 + pressureTier(dayTime, distanceFromSpawn) * 8;
    }

    public static int scaleEventDelayTicks(int baseDelayTicks, double frequencyMultiplier) {
        if (baseDelayTicks <= 0) return 1;
        double frequency = Double.isFinite(frequencyMultiplier)
                ? Math.max(0.1D, Math.min(3.0D, frequencyMultiplier)) : 1.0D;
        return Math.max(1, (int) Math.round(baseDelayTicks / frequency));
    }

    public static boolean shouldReplaceTarget(boolean hasTarget, boolean targetAlive,
                                              boolean targetIsPlayer, boolean sameLevel,
                                              boolean creative, boolean spectator) {
        return !hasTarget || !targetAlive
                || (targetIsPlayer && (!sameLevel || creative || spectator));
    }

    public static boolean watcherShouldFreeze(double distanceSquared, double lookDot, boolean lineOfSight) {
        return lineOfSight && Double.isFinite(distanceSquared) && Double.isFinite(lookDot)
                && distanceSquared >= 0.0D && distanceSquared <= 1_600.0D && lookDot >= 0.78D;
    }

    public static int nextEventDelayTicks(long dayTime, double distanceFromSpawn, long randomBits) {
        int tier = pressureTier(dayTime, distanceFromSpawn);
        int minimum = Math.max(600, 1_500 - tier * 170);
        int spread = Math.max(720, 2_100 - tier * 240);
        return minimum + (int) Long.remainderUnsigned(randomBits, spread);
    }
}
