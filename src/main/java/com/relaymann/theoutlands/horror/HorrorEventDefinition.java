package com.relaymann.theoutlands.horror;

public record HorrorEventDefinition(
        String id,
        double minimumDistance,
        double minimumPressure,
        int weight,
        long cooldownTicks,
        double pressureCost
) {
    public HorrorEventDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event id must not be blank");
        }
        if (!Double.isFinite(minimumDistance) || minimumDistance < 0.0D) {
            throw new IllegalArgumentException("Minimum distance must be finite and non-negative");
        }
        if (!Double.isFinite(minimumPressure) || minimumPressure < 0.0D) {
            throw new IllegalArgumentException("Minimum pressure must be finite and non-negative");
        }
        if (weight < 1) {
            throw new IllegalArgumentException("Event weight must be positive");
        }
        if (cooldownTicks < 0L) {
            throw new IllegalArgumentException("Cooldown must not be negative");
        }
        if (!Double.isFinite(pressureCost) || pressureCost < 0.0D) {
            throw new IllegalArgumentException("Pressure cost must be finite and non-negative");
        }
    }
}
