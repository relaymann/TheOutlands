package com.relaymann.theoutlands.horror;

/** Bounded adaptation of Null's distance and persistence to prior encounters. */
public record NullStalkingProfile(long lifetimeTicks, long relocationIntervalTicks,
                                  double minimumDistance, double maximumDistance) {
    public static NullStalkingProfile fromPreviousSightings(int previousSightings) {
        int experience = Math.max(0, Math.min(12, previousSightings));
        long lifetime = Math.max(140L, 260L - experience * 10L);
        long relocation = Math.max(35L, 75L - experience * 4L);
        double minimum = Math.max(16.0D, 34.0D - experience * 1.5D);
        return new NullStalkingProfile(lifetime, relocation, minimum, minimum + 12.0D);
    }
}
