package com.relaymann.theoutlands.horror;

/** Very sparse, distance-scaled jumpscare pacing for aggressive entities. */
public final class AggressiveJumpscarePolicy {
    public static final double MINIMUM_DISTANCE = 11_000.0D;
    public static final long COOLDOWN_TICKS = 36_000L;

    private AggressiveJumpscarePolicy() {}

    public static boolean isUnlocked(double distance) {
        return Double.isFinite(distance) && distance >= MINIMUM_DISTANCE;
    }

    /** Probability per 20-second progression evaluation; capped to keep scares uncommon. */
    public static double chancePerEvaluation(double distance) {
        if (!isUnlocked(distance)) return 0.0D;
        double progress = Math.max(0.0D, Math.min(1.0D,
                (distance - MINIMUM_DISTANCE) / 89_000.0D));
        return 0.0008D + progress * 0.0022D;
    }

    public static boolean shouldAttempt(double distance, boolean fearIntroduced,
                                        boolean deathIntroduced, boolean inAlmightyTerritory,
                                        long ticksSinceLastScare, double roll) {
        if (!isUnlocked(distance) || (!fearIntroduced && !deathIntroduced)
                || inAlmightyTerritory || ticksSinceLastScare < COOLDOWN_TICKS
                || !Double.isFinite(roll) || roll < 0.0D || roll >= 1.0D) return false;
        return roll < chancePerEvaluation(distance);
    }
}
