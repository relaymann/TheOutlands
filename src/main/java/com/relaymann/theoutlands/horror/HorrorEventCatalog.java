package com.relaymann.theoutlands.horror;

import java.util.Set;

/** Pure catalog of encounter IDs; safe to load in the isolated unit-test runtime. */
public final class HorrorEventCatalog {
    private static final Set<String> IDS = Set.of(
            "ambient_displacement", "false_footsteps", "distant_figure", "unexplained_message",
            "wrong_sounds", "observer", "environmental_distortion", "missing_terrain",
            "fake_structure", "the_return", "repeated_location", "silent_world",
            "entity_imitation", "false_death", "distant_scream", "false_player",
            "direct_intervention", "fear_whisper", "fear_presence", "fear_call",
            "familiar_echo", "environmental_hush", "almighty_influence", "world_inconsistency",
            "false_structure", "paranoia_echo", "null_apostle_sign");

    private HorrorEventCatalog() {}
    public static Set<String> ids() { return IDS; }
}
