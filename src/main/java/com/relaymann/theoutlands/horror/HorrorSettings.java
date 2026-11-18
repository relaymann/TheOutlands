package com.relaymann.theoutlands.horror;

/** Immutable, test-friendly settings snapshot for one director evaluation. */
public record HorrorSettings(double intensity, double eventFrequency, boolean eventsEnabled) {
    public static final HorrorSettings DEFAULT = new HorrorSettings(1.0D, 1.0D, true);

    public HorrorSettings {
        intensity = Double.isFinite(intensity) ? Math.max(0.1D, Math.min(2.5D, intensity)) : 1.0D;
        eventFrequency = Double.isFinite(eventFrequency) ? Math.max(0.1D, Math.min(3.0D, eventFrequency)) : 1.0D;
    }
}
