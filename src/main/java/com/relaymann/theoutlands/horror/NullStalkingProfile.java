package com.relaymann.theoutlands.horror;

/** Bounded adaptation of Null's sighting distance and timing to prior encounters. */
public record NullStalkingProfile(long lifetimeTicks, long relocationIntervalTicks,
                                  double minimumDistance, double maximumDistance) {
    public static NullStalkingProfile fromPreviousSightings(int previousSightings) {
        int experience = Math.max(0, Math.min(12, previousSightings));
        long lifetime = Math.max(140L, 260L - experience * 10L);
        long relocation = Math.max(35L, 75L - experience * 4L);
        double minimum = Math.max(12.0D, 20.0D - experience * 0.75D);
        return new NullStalkingProfile(lifetime, relocation, minimum, minimum + 8.0D);
    }
}
