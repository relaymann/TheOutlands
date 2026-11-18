package com.relaymann.theoutlands.horror;

/** Player-local conditions and remembered history supplied to the Horror Director. */
public record HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                            int nearbyPlayers, float healthFraction,
                            int previousEncounterCount, boolean hasSeenNull) {
    public HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                         int nearbyPlayers, float healthFraction) {
        this(biomeId, night, raining, thundering, nearbyPlayers, healthFraction, 0, false);
    }

    public HorrorContext {
        biomeId = biomeId == null ? "unknown" : biomeId;
        nearbyPlayers = Math.max(0, nearbyPlayers);
        healthFraction = Float.isFinite(healthFraction) ? Math.max(0.0F, Math.min(1.0F, healthFraction)) : 1.0F;
        previousEncounterCount = Math.max(0, previousEncounterCount);
    }

    public static HorrorContext neutral() {
        return new HorrorContext("unknown", false, false, false, 0, 1.0F, 0, false);
    }

    public double weightMultiplier(String id) {
        double weight = 1.0D;
        if ("distant_figure".equals(id) && night) weight *= 1.55D;
        if ("distant_figure".equals(id) && hasSeenNull) weight *= 0.65D;
        if ("environmental_distortion".equals(id) && (raining || thundering)) weight *= 1.45D;
        if ("hostile_warning".equals(id) && healthFraction <= 0.35F) weight *= 1.5D;
        if ("direct_intervention".equals(id) && nearbyPlayers > 0) weight *= 0.8D;
        if ("ambient_displacement".equals(id) && biomeId.contains("forest")) weight *= 1.2D;
        if ("false_death".equals(id) && healthFraction < 0.5F) weight *= 1.2D;
        if ("false_death".equals(id) && previousEncounterCount < 3) weight *= 0.5D;
        if ("the_return".equals(id) && nearbyPlayers == 0) weight *= 1.25D;
        if ("the_return".equals(id) && hasSeenNull) weight *= 1.6D;
        if ("unexplained_message".equals(id) && night) weight *= 1.2D;
        if ("unexplained_message".equals(id) && previousEncounterCount >= 5) weight *= 1.25D;
        if ("wrong_sounds".equals(id) && night) weight *= 1.35D;
        if ("observer".equals(id) && biomeId.contains("forest")) weight *= 1.4D;
        if ("missing_terrain".equals(id) && thundering) weight *= 1.35D;
        if ("fake_structure".equals(id) && previousEncounterCount >= 4) weight *= 1.25D;
        if ("silent_world".equals(id) && nearbyPlayers == 0) weight *= 1.5D;
        if ("entity_imitation".equals(id) && nearbyPlayers > 0) weight *= 1.15D;
        if ("distant_scream".equals(id) && (night || biomeId.contains("forest"))) weight *= 1.4D;
        if ("false_player".equals(id) && hasSeenNull) weight *= 1.3D;
        return weight;
    }
}
