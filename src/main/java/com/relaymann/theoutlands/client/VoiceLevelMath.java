package com.relaymann.theoutlands.client;

/** Converts a local 16-bit PCM microphone frame into a stable 0-100 activity meter. */
public final class VoiceLevelMath {
    private VoiceLevelMath() {}

    public static int fromPcm(short[] samples) {
        if (samples == null || samples.length == 0) return 0;
        double sum = 0.0D;
        for (short sample : samples) {
            double normalized = sample / 32768.0D;
            sum += normalized * normalized;
        }
        double rms = Math.sqrt(sum / samples.length);
        double dbfs = rms <= 0.000001D ? -120.0D : 20.0D * Math.log10(rms);
        return (int) Math.round(Math.max(0.0D,
                Math.min(100.0D, (dbfs + 60.0D) * (100.0D / 54.0D))));
    }
}
