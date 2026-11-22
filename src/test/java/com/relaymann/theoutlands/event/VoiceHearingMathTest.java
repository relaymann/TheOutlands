package com.relaymann.theoutlands.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceHearingMathTest {
    private static final double BASE = 52.0D;

    @Test
    void louderSpeechIsHeardFartherThanQuietSpeech() {
        double quiet = VoiceHearingMath.effectiveRange(BASE, 1.0D, 10, false, true, false, false, false);
        double loud = VoiceHearingMath.effectiveRange(BASE, 1.0D, 100, false, true, false, false, false);
        assertTrue(loud > quiet);
    }

    @Test
    void whisperingReducesDetectionRange() {
        double normal = VoiceHearingMath.effectiveRange(BASE, 1.0D, 80, false, true, false, false, false);
        double whisper = VoiceHearingMath.effectiveRange(BASE, 1.0D, 80, true, true, false, false, false);
        assertTrue(whisper < normal);
    }

    @Test
    void obstructionAndWeatherReduceDetectionRange() {
        double clear = VoiceHearingMath.effectiveRange(BASE, 1.0D, 70, false, true, false, false, false);
        double obstructed = VoiceHearingMath.effectiveRange(BASE, 1.0D, 70, false, false, false, false, false);
        double rainy = VoiceHearingMath.effectiveRange(BASE, 1.0D, 70, false, true, true, false, false);
        double stormy = VoiceHearingMath.effectiveRange(BASE, 1.0D, 70, false, true, true, true, false);
        assertTrue(obstructed < clear);
        assertTrue(rainy < clear);
        assertTrue(stormy < rainy);
    }

    @Test
    void specializedHearingNeverExceedsConfiguredHardCap() {
        double range = VoiceHearingMath.effectiveRange(BASE, 2.0D, 100, false, true, false, false, true);
        assertEquals(BASE * 1.65D, range, 0.0001D);
    }

    @Test
    void invalidConfigurationCannotProduceNegativeOrInfiniteRange() {
        assertEquals(0.0D, VoiceHearingMath.effectiveRange(-1.0D, 1.0D, 100, false, true, false, false, false));
        assertEquals(0.0D, VoiceHearingMath.effectiveRange(BASE, Double.NaN, 100, false, true, false, false, false));
    }

    @Test
    void microphoneLevelIsClampedToZeroThroughOneHundred() {
        double low = VoiceHearingMath.effectiveRange(BASE, 1.0D, -500, false, true, false, false, false);
        double zero = VoiceHearingMath.effectiveRange(BASE, 1.0D, 0, false, true, false, false, false);
        double high = VoiceHearingMath.effectiveRange(BASE, 1.0D, 500, false, true, false, false, false);
        double hundred = VoiceHearingMath.effectiveRange(BASE, 1.0D, 100, false, true, false, false, false);
        assertEquals(zero, low, 0.0001D);
        assertEquals(hundred, high, 0.0001D);
    }
}
