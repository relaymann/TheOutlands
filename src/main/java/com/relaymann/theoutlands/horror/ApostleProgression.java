package com.relaymann.theoutlands.horror;

/**
 * Canonical distance milestones for the apostle storyline.
 * These are story gates, not a cap on general horror escalation.
 */
public final class ApostleProgression {
    public static final double FEAR_FIRST_APPEARANCE_DISTANCE = 15_000.0D;
    public static final double DEATH_FIRST_APPEARANCE_DISTANCE = 50_000.0D;
    public static final double FEAR_FULL_TERRITORY_DISTANCE = 100_000.0D;

    private ApostleProgression() {
    }

    public static Snapshot evaluate(double distanceFromSpawn, boolean almightyTerritory) {
        return evaluate(distanceFromSpawn, almightyTerritory,
                FEAR_FIRST_APPEARANCE_DISTANCE, DEATH_FIRST_APPEARANCE_DISTANCE,
                FEAR_FULL_TERRITORY_DISTANCE);
    }

    public static Snapshot evaluate(double distanceFromSpawn, boolean almightyTerritory,
                                    double fearFirstAppearance, double deathFirstAppearance,
                                    double fearFullTerritory) {
        double distance = Double.isFinite(distanceFromSpawn) ? Math.max(0.0D, distanceFromSpawn) : 0.0D;
        boolean deathUnlocked = distance >= deathFirstAppearance;
        return new Snapshot(true, distance >= fearFirstAppearance,
                distance >= fearFullTerritory, deathUnlocked,
                deathUnlocked && !almightyTerritory, almightyTerritory);
    }

    public record Snapshot(
            boolean nullMayAppear,
            boolean fearIntroductionUnlocked,
            boolean fearFullTerritoryUnlocked,
            boolean deathIntroductionUnlocked,
            boolean deathTerritoryActive,
            boolean almightyTerritoryActive
    ) {
    }
}
