package com.relaymann.theoutlands.horror;

import com.relaymann.theoutlands.OutlandsConfig;

/**
 * Canonical distance milestones for the apostle storyline.
 *
 * <p>These milestones unlock story/territory systems; they do not cap the general
 * horror escalation. The Almighty's border region always takes precedence over
 * Death's territory. Fear's forest territory is a distinct biome region that may
 * exist as an enclave within the broader outer progression.</p>
 */
public final class ApostleProgression {
    public static final double FEAR_FIRST_APPEARANCE_DISTANCE = 15_000.0D;
    public static final double DEATH_FIRST_APPEARANCE_DISTANCE = 50_000.0D;
    public static final double FEAR_FULL_TERRITORY_DISTANCE = 100_000.0D;

    private ApostleProgression() {
    }

    public static Snapshot evaluate(double distanceFromSpawn, boolean almightyTerritory) {
        double distance = Double.isFinite(distanceFromSpawn) ? Math.max(0.0D, distanceFromSpawn) : 0.0D;
        boolean almighty = almightyTerritory;
        boolean deathUnlocked = distance >= OutlandsConfig.DEATH_FIRST_APPEARANCE_DISTANCE.get();
        return new Snapshot(
                true,
                distance >= OutlandsConfig.FEAR_FIRST_APPEARANCE_DISTANCE.get(),
                distance >= OutlandsConfig.FEAR_FULL_TERRITORY_DISTANCE.get(),
                deathUnlocked,
                deathUnlocked && !almighty,
                almighty
        );
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
