package com.relaymann.theoutlands.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceLevelMathTest {
    @Test
    void silenceAndEmptyFramesProduceZero() {
        assertEquals(0, VoiceLevelMath.fromPcm(new short[128]));
        assertEquals(0, VoiceLevelMath.fromPcm(new short[0]));
        assertEquals(0, VoiceLevelMath.fromPcm(null));
    }

    @Test
    void fullScaleSignalClampsToOneHundred() {
        short[] samples = new short[64];
        java.util.Arrays.fill(samples, Short.MAX_VALUE);
        assertEquals(100, VoiceLevelMath.fromPcm(samples));
    }

    @Test
    void ordinaryMicrophoneSignalProducesIntermediateLevel() {
        short[] samples = new short[128];
        java.util.Arrays.fill(samples, (short) 3276);
        int level = VoiceLevelMath.fromPcm(samples);
        assertTrue(level >= 70 && level <= 78, "Expected a readable mid/high meter, got " + level);
    }

    @Test
    void veryQuietNoiseFallsBelowTheMeterFloor() {
        short[] samples = new short[64];
        java.util.Arrays.fill(samples, (short) 8);
        assertEquals(0, VoiceLevelMath.fromPcm(samples));
    }

    @Test
    void calculationDoesNotModifyMicrophoneSamples() {
        short[] samples = {0, 512, -1024, 2048, -4096};
        short[] original = samples.clone();
        VoiceLevelMath.fromPcm(samples);
        assertArrayEquals(original, samples);
    }
}
