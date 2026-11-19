package com.relaymann.theoutlands.horror;

/** Player-local conditions and remembered history supplied to the Horror Director. */
public record HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                            int nearbyPlayers, float healthFraction,
                            int previousEncounterCount, boolean hasSeenNull, boolean hasMetFear,
                            int lookBackCount, int pauseCount, boolean nearAlmightyTerritory) {
    public HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                         int nearbyPlayers, float healthFraction) {
        this(biomeId, night, raining, thundering, nearbyPlayers, healthFraction, 0, false, false, 0, 0, false);
    }

    public HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                         int nearbyPlayers, float healthFraction, int previousEncounterCount, boolean hasSeenNull) {
        this(biomeId, night, raining, thundering, nearbyPlayers, healthFraction,
                previousEncounterCount, hasSeenNull, false, 0, 0, false);
    }

    public HorrorContext(String biomeId, boolean night, boolean raining, boolean thundering,
                         int nearbyPlayers, float healthFraction, int previousEncounterCount,
                         boolean hasSeenNull, boolean hasMetFear) {
        this(biomeId, night, raining, thundering, nearbyPlayers, healthFraction,
                previousEncounterCount, hasSeenNull, hasMetFear, 0, 0, false);
    }

    public HorrorContext {
        biomeId = biomeId == null ? "unknown" : biomeId;
        nearbyPlayers = Math.max(0, nearbyPlayers);
        healthFraction = Float.isFinite(healthFraction) ? Math.max(0.0F, Math.min(1.0F, healthFraction)) : 1.0F;
        previousEncounterCount = Math.max(0, previousEncounterCount);
        lookBackCount = Math.max(0, lookBackCount);
        pauseCount = Math.max(0, pauseCount);
    }

    public static HorrorContext neutral() {
        return new HorrorContext("unknown", false, false, false, 0, 1.0F, 0, false, false, 0, 0, false);
    }

    public double weightMultiplier(String id) {
        double weight = 1.0D;
        if ("distant_figure".equals(id) && night) weight *= 1.55D;
        if ("distant_figure".equals(id) && hasSeenNull) weight *= 0.65D;
        if ("environmental_distortion".equals(id) && (raining || thundering)) weight *= 1.45D;
        if ("direct_intervention".equals(id) && nearbyPlayers > 0) weight *= 0.8D;
        if ("ambient_displacement".equals(id) && biomeId.contains("forest")) weight *= 1.2D;
        if ("false_death".equals(id) && healthFraction < 0.5F) weight *= 1.2D;
        if ("false_death".equals(id) && previousEncounterCount < 3) weight *= 0.5D;
        if ("the_return".equals(id) && nearbyPlayers == 0) weight *= 1.25D;
        if ("the_return".equals(id) && hasSeenNull) weight *= 1.6D;
        if ("wrong_sounds".equals(id) && night) weight *= 1.35D;
        if ("observer".equals(id) && biomeId.contains("forest")) weight *= 1.4D;
        // Isolation and familiar routines make subtle events more likely, not louder or more frequent.
        if (nearbyPlayers == 0 && ("wall_knock".equals(id) || "behind_steps".equals(id)
                || "breathing_presence".equals(id) || "familiar_echo".equals(id))) weight *= 1.22D;
        if (night && ("wall_knock".equals(id) || "door_afterimage".equals(id)
                || "false_wakeup".equals(id))) weight *= 1.18D;
        if (raining && ("wrong_sounds".equals(id) || "false_footsteps".equals(id))) weight *= 1.18D;
        if (thundering && ("environmental_distortion".equals(id) || "world_inconsistency".equals(id))) weight *= 1.2D;
        if (healthFraction < 0.35F && ("false_death".equals(id) || "environmental_hush".equals(id))) weight *= 0.85D;
        if (previousEncounterCount >= 8 && ("fake_structure".equals(id) || "false_structure".equals(id)
                || "world_inconsistency".equals(id))) weight *= 1.18D;
        if (previousEncounterCount >= 15 && ("paranoia_echo".equals(id) || "familiar_echo".equals(id))) weight *= 1.22D;
        if ("missing_terrain".equals(id) && thundering) weight *= 1.35D;
        if ("fake_structure".equals(id) && previousEncounterCount >= 4) weight *= 1.25D;
        if ("silent_world".equals(id) && nearbyPlayers == 0) weight *= 1.5D;
        if ("entity_imitation".equals(id) && nearbyPlayers > 0) weight *= 1.15D;
        if ("distant_scream".equals(id) && (night || biomeId.contains("forest"))) weight *= 1.4D;
        if ("false_player".equals(id) && hasSeenNull) weight *= 1.3D;
        if (id.startsWith("fear_") && hasMetFear && night) weight *= 1.25D;
        if ("fear_presence".equals(id) && nearbyPlayers == 0) weight *= 1.2D;
        if ("familiar_echo".equals(id) && (lookBackCount >= 3 || pauseCount >= 8)) weight *= 1.7D;
        if ("environmental_hush".equals(id) && nearbyPlayers == 0) weight *= 1.35D;
        if ("almighty_influence".equals(id) && nearAlmightyTerritory) weight *= 1.0D;
        if ("world_inconsistency".equals(id) && previousEncounterCount >= 4) weight *= 1.3D;
        if ("false_structure".equals(id) && biomeId.contains("forest")) weight *= 1.5D;
        if ("paranoia_echo".equals(id) && (lookBackCount >= 4 || pauseCount >= 6)) weight *= 1.6D;
        if ("null_apostle_sign".equals(id) && hasSeenNull) weight *= 1.8D;
        return weight;
    }
}
