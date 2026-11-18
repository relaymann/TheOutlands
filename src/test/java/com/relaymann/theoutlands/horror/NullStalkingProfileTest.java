package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NullStalkingProfileTest {
    @Test
    void firstSightingsCanOccurAtLongerDistances() {
        var profile = NullStalkingProfile.fromPreviousSightings(0);
        assertEquals(260L, profile.lifetimeTicks());
        assertEquals(75L, profile.relocationIntervalTicks());
        assertEquals(34.0D, profile.minimumDistance());
        assertEquals(46.0D, profile.maximumDistance());
    }

    @Test
    void repeatedSightingsBecomeCloserAndMorePersistent() {
        var first = NullStalkingProfile.fromPreviousSightings(0);
        var repeated = NullStalkingProfile.fromPreviousSightings(6);
        assertTrue(repeated.minimumDistance() < first.minimumDistance());
        assertTrue(repeated.maximumDistance() < first.maximumDistance());
        assertTrue(repeated.lifetimeTicks() < first.lifetimeTicks());
        assertTrue(repeated.relocationIntervalTicks() < first.relocationIntervalTicks());
    }

    @Test
    void behaviorChangesNullsDistanceAndRelocationPattern() {
        var calm = NullStalkingProfile.fromPlayerHistory(0, 0, 0, 0);
        var watched = NullStalkingProfile.fromPlayerHistory(0, 10, 10, 20);
        assertTrue(watched.minimumDistance() < calm.minimumDistance());
        assertTrue(watched.relocationIntervalTicks() < calm.relocationIntervalTicks());
        assertTrue(watched.lifetimeTicks() >= calm.lifetimeTicks());
    }

    @Test
    void adaptationHasSafeBounds() {
        var repeated = NullStalkingProfile.fromPreviousSightings(Integer.MAX_VALUE);
        assertEquals(140L, repeated.lifetimeTicks());
        assertEquals(35L, repeated.relocationIntervalTicks());
        assertEquals(16.0D, repeated.minimumDistance());
        assertEquals(28.0D, repeated.maximumDistance());
        assertEquals(NullStalkingProfile.fromPreviousSightings(12),
                NullStalkingProfile.fromPreviousSightings(500));
    }
}
