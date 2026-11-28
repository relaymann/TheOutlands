package com.relaymann.theoutlands.horror;

public record NullStalkingProfile(long lifetimeTicks, long relocationIntervalTicks,
                                  double minimumDistance, double maximumDistance) {
    public static NullStalkingProfile fromPreviousSightings(int previousSightings) {
        int experience = Math.max(0, Math.min(12, previousSightings));
        long lifetime = Math.max(140L, 260L - experience * 10L);
        long relocation = Math.max(35L, 75L - experience * 4L);
        double minimum = Math.max(16.0D, 34.0D - experience * 1.5D);
        return new NullStalkingProfile(lifetime, relocation, minimum, minimum + 12.0D);
    }

    public static NullStalkingProfile fromPlayerHistory(int previousSightings, int lookBacks, int pauses) {
        return fromPlayerHistory(previousSightings, lookBacks, pauses, 0);
    }

    public static NullStalkingProfile fromPlayerHistory(int previousSightings, int lookBacks,
                                                        int pauses, int flightSamples) {
        int experience = Math.max(0, Math.min(12, previousSightings));
        int awareness = Math.max(0, Math.min(8, lookBacks / 2 + pauses / 5 + flightSamples / 10));
        long lifetime = Math.max(140L, 260L - experience * 10L + awareness * 3L);
        long relocation = Math.max(30L, 75L - experience * 4L - awareness * 2L);
        double minimum = Math.max(12.0D, 34.0D - experience * 1.5D - awareness * 1.25D);
        return new NullStalkingProfile(lifetime, relocation, minimum, minimum + 12.0D);
    }
}
