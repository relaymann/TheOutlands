package com.relaymann.theoutlands.event;

public final class VoiceHearingMath {
    private VoiceHearingMath() {}

    public static int normalizeLevel(int level, boolean whispering) {
        int clamped = Math.max(0, Math.min(100, level));
        return whispering ? Math.min(38, clamped) : clamped;
    }

    public static double effectiveRange(double baseRange, double speciesScale, int level,
                                        boolean whispering, boolean lineOfSight,
                                        boolean raining, boolean thundering, boolean underground) {
        if (!Double.isFinite(baseRange) || baseRange <= 0.0D
                || !Double.isFinite(speciesScale) || speciesScale <= 0.0D) return 0.0D;
        int clampedLevel = Math.max(0, Math.min(100, level));
        double range = baseRange * speciesScale;
        range *= 0.25D + (clampedLevel / 100.0D) * 0.75D;
        if (whispering) range *= 0.48D;
        else if (clampedLevel >= 76) range *= 1.18D;
        if (!lineOfSight) range *= 0.68D;
        if (raining) range *= 0.88D;
        if (thundering) range *= 0.92D;
        if (underground) range *= 1.28D;
        return Math.min(range, baseRange * 1.65D);
    }
}
