package com.relaymann.theoutlands.horror;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NullStalkingProfileTest {
    @Test
    void firstSightingsAreDistantAndBrief() {
        var profile = NullStalkingProfile.fromPreviousSightings(0);
        assertEquals(260L, profile.lifetimeTicks());
        assertEquals(75L, profile.relocationIntervalTicks());
        assertEquals(20.0D, profile.minimumDistance());
        assertEquals(28.0D, profile.maximumDistance());
    }

    @Test
    void repeatedSightingsBecomeCloserAndMorePersistent() {
        var first = NullStalkingProfile.fromPreviousSightings(0);
        var repeated = NullStalkingProfile.fromPreviousSightings(6);
        assertTrue(repeated.minimumDistance() < first.minimumDistance());
        assertTrue(repeated.lifetimeTicks() < first.lifetimeTicks());
        assertTrue(repeated.relocationIntervalTicks() < first.relocationIntervalTicks());
    }

    @Test
    void adaptationHasSafeBounds() {
        var repeated = NullStalkingProfile.fromPreviousSightings(Integer.MAX_VALUE);
        assertEquals(140L, repeated.lifetimeTicks());
        assertEquals(35L, repeated.relocationIntervalTicks());
        assertEquals(12.0D, repeated.minimumDistance());
        assertEquals(20.0D, repeated.maximumDistance());
        assertEquals(NullStalkingProfile.fromPreviousSightings(12),
                NullStalkingProfile.fromPreviousSightings(500));
    }
}
